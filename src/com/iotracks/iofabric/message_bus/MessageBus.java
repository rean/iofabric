package com.iotracks.iofabric.message_bus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.iotracks.iofabric.element.ElementManager;
import com.iotracks.iofabric.element.Route;
import com.iotracks.iofabric.status_reporter.StatusReporter;
import com.iotracks.iofabric.utils.Constants;
import com.iotracks.iofabric.utils.Constants.ModulesStatus;
import com.iotracks.iofabric.utils.configuration.Configuration;
import com.iotracks.iofabric.utils.logging.LoggingService;

public class MessageBus {
	
	private final String MODULE_NAME = "Message Bus";
	private final int SPEED_CALCULATION_FREQ_MINUTES = 5;
	
	private MessageBusServer messageBusServer;
	private Map<String, Route> routes;
	private Map<String, MessagePublisher> publishers;
	private Map<String, MessageReceiver> receivers;
	private MessageIdGenerator idGenerator;
	
	public MessageBus() {
	}
	
	public Message publishMessage(Message message) {
		long timestamp = System.currentTimeMillis();
		StatusReporter.setMessageBusStatus().increasePublishedMessagesPerElement(message.getPublisher());
		message.setId(idGenerator.generate(timestamp));
		message.setTimestamp(timestamp);
		
		MessagePublisher publisher = publishers.get(message.getPublisher());

		if (publisher != null)
			publisher.publish(message);

		return message;
	}
	
	public List<Message> getMessages(String receiver) {
		MessageReceiver rec = receivers.get(receiver); 
		if (rec == null)
			return null;
		return rec.getMessages();
	}
	
	public void enableRealTimeReceiving(String receiver) {
		MessageReceiver rec = receivers.get(receiver); 
		if (rec == null)
			return;
		rec.enableRealTimeReceiving();
	}

	public void disableRealTimeReceiving(String receiver) {
		MessageReceiver rec = receivers.get(receiver); 
		if (rec == null)
			return;
		rec.disableRealTimeReceiving();
	}

	private void init() {
		if (routes == null)
			return;
		
		routes.entrySet().forEach(entry -> {
			if (entry.getValue() != null && entry.getValue().getReceivers() != null) {
				publishers.put(entry.getKey(), new MessagePublisher(entry.getKey(), entry.getValue()));
				for (String receiver : entry.getValue().getReceivers())
					if (!receivers.containsKey(receiver)) {
						messageBusServer.createCosumer(receiver);
						receivers.put(receiver, new MessageReceiver(receiver));
					}
			}
		});
	}
	
	public synchronized void updateRoutes() {
		Map<String, Route> newRoutes = ElementManager.getRoutes();
		List<String> newPublishers = new ArrayList<>();
		List<String> newReceivers = new ArrayList<>();
		if (newRoutes != null) {
			newRoutes.entrySet().forEach(entry -> {
				if (entry.getValue() != null && entry.getValue().getReceivers() != null) {
					newPublishers.add(entry.getKey());
					for (String receiver : entry.getValue().getReceivers())
						if (!newReceivers.contains(receiver)) {
							newReceivers.add(receiver);
						}
				}
			});
		}
		List<String> toRemove = new ArrayList<>();

		publishers.entrySet().forEach(entry -> {
			if (!newPublishers.contains(entry.getKey())) {
				entry.getValue().close();
				toRemove.add(entry.getKey());
			}
		});
		toRemove.forEach(publisher -> {
			publishers.remove(publisher);
		});
		toRemove.clear();
		newPublishers.forEach(publisher -> {
			if (!publishers.containsKey(publisher))
				publishers.put(publisher, new MessagePublisher(publisher, newRoutes.get(publisher)));
		});
		
		receivers.entrySet().forEach(entry -> {
			if (!newReceivers.contains(entry.getKey())) {
				entry.getValue().close();
				toRemove.add(entry.getKey());
			}
		});
		toRemove.forEach(receiver -> {
			receivers.remove(receiver);
		});
		toRemove.clear();
		newReceivers.forEach(receiver -> {
			if (!receivers.containsKey(receiver))
				receivers.put(receiver, new MessageReceiver(receiver));
		});
		
		routes = newRoutes;
	}
	
	private final Runnable calculateSpeed = () -> {
		long now = System.currentTimeMillis();
		long msgs = StatusReporter.getMessageBusStatus().getProcessedMessages();
		
		float speed = ((msgs * 1.0f) / now) * 1000;
		speed = (speed + StatusReporter.getMessageBusStatus().getAverageSpeed()) / 2.0f;
		StatusReporter.setMessageBusStatus().setAverageSpeed(speed);
	};
	
	public void start() {
		StatusReporter.setSupervisorStatus().setModuleStatus(Constants.MESSAGE_BUS, ModulesStatus.STARTING);
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(calculateSpeed, 0, SPEED_CALCULATION_FREQ_MINUTES, TimeUnit.MINUTES);

		messageBusServer = new MessageBusServer();
		try {
			messageBusServer.startServer();
			messageBusServer.initialize();
		} catch (Exception e) {
			try {
				messageBusServer.stopServer();
			} catch (Exception e1) {}
			LoggingService.logWarning(MODULE_NAME, "unable to start message bus server\n" + e.getMessage());
			StatusReporter.setSupervisorStatus().setModuleStatus(Constants.MESSAGE_BUS, ModulesStatus.STOPPED);
			return;
		}
		
		idGenerator = new MessageIdGenerator();
		publishers = new ConcurrentHashMap<>();
		receivers = new ConcurrentHashMap<>();
		
		routes = ElementManager.getRoutes();
		
		init();

		
		
		
		String p = "DTCnTG4dLyrGC7XYrzzTqNhW7R78hk3V";
		String r = "wF8VmXTQcyBRPhb27XKgm4gpq97NN2bh";
//		enableRealTimeReceiving(r);
		
		Message m = new Message();
		m.setTag("BB");
		m.setMessageGroupId("CC");
		m.setSequenceNumber(1);
		m.setSequenceTotal(2);
		m.setPriority((byte) 3);
		m.setPublisher(p);
		m.setAuthIdentifier("EE");
		m.setAuthGroup("FF");
		m.setChainPosition(5);
		m.setHash("GG");
		m.setPreviousHash("HH");
		m.setNonce("II");
		m.setDifficultyTarget(6);
		m.setInfoType("JJ");
		m.setInfoFormat("KK");
		m.setContextData(new byte[] {7, 7, 7, 7, 7});
		m.setContentData(new byte[] {8, 8, 8, 8, 8});
		System.out.println(m.toString());

		long start = System.currentTimeMillis();
		for (int i = 0; i < 1001; i++) {
			publishMessage(m);
		}
		System.out.println(System.currentTimeMillis() - start);

		List<Message> messages = getMessages(r);
		for (Message message : messages) {
			System.out.println(message);
		}
		updateRoutes();
		
		
		for (MessagePublisher publisher : publishers.values())
			publisher.close();
		try {
			messageBusServer.stopServer();
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}
	
	public static void main(String[] args) throws Exception {
		Configuration.loadConfig();
		new ElementManager().loadFromApi();
		try {
			LoggingService.setupLogger();
		} catch (IOException e) {
			System.out.println("Error starting logging service\n" + e.getMessage());
			System.exit(1);
		}
		LoggingService.logInfo("Main", "configuration loaded.");
		MessageBus messageBus = new MessageBus();
		messageBus.start();
		System.exit(0);
	}
}
