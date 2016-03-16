package com.iotracks.iofabric.message_bus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.iotracks.iofabric.element.ElementManager;
import com.iotracks.iofabric.element.Route;
import com.iotracks.iofabric.status_reporter.StatusReporter;
import com.iotracks.iofabric.supervisor.Supervisor;
import com.iotracks.iofabric.utils.Constants;
import com.iotracks.iofabric.utils.Constants.ModulesStatus;
import com.iotracks.iofabric.utils.configuration.Configuration;
import com.iotracks.iofabric.utils.logging.LoggingService;

public class MessageBus {
	
	private final String MODULE_NAME = "Message Bus";
	private final int SPEED_CALCULATION_FREQ_MINUTES = 1;
	
	private MessageBusServer messageBusServer;
	private Map<String, Route> routes;
	private Map<String, MessagePublisher> publishers;
	private Map<String, MessageReceiver> receivers;
	private MessageIdGenerator idGenerator;
	private static MessageBus instance;
	private ElementManager elementManager;
	
	private MessageBus() {
	}
	
	public static MessageBus getInstance() {
		if (instance == null) {
			synchronized (MessageBus.class) {
				if (instance == null) { 
					instance = new MessageBus();
					instance.start();
				}
			}
		}
		return instance;
	}
	
	
	
	public Message publishMessage(Message message) {
		long timestamp = System.currentTimeMillis();
		StatusReporter.setMessageBusStatus().increasePublishedMessagesPerElement(message.getPublisher());
		message.setId(idGenerator.getNextId());
		message.setTimestamp(timestamp);
		
		MessagePublisher publisher = publishers.get(message.getPublisher());

		if (publisher != null)
			try {
				publisher.publish(message);
			} catch (Exception e) {
				LoggingService.logWarning(MODULE_NAME + "(" + message.getPublisher() + ")", "unable to send message --> " + e.getMessage());
			}

		return message;
	}
	
	public List<Message> getMessages(String receiver) {
		MessageReceiver rec = receivers.get(receiver); 
		if (rec == null)
			return null;
		List<Message> messages = new ArrayList<>();
		try {
			messages = rec.getMessages();
		} catch (Exception e) {
			LoggingService.logWarning(MODULE_NAME + "(" + receiver + ")", "unable to receive messages --> " + e.getMessage());
		} 
		return messages;
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
		routes = elementManager.getRoutes();
		idGenerator = new MessageIdGenerator();
		publishers = new ConcurrentHashMap<>();
		receivers = new ConcurrentHashMap<>();

		if (routes == null)
			return;
		
		routes.entrySet().forEach(entry -> {
			if (entry.getValue() != null && entry.getValue().getReceivers() != null) {
				publishers.put(entry.getKey(), new MessagePublisher(entry.getKey(), entry.getValue()));
				for (String receiver : entry.getValue().getReceivers())
					if (!receivers.containsKey(receiver)) {
						try {
							messageBusServer.createCosumer(receiver);
							receivers.put(receiver, new MessageReceiver(receiver));
						} catch (Exception e) {
							LoggingService.logWarning(MODULE_NAME + "(" + receiver + ")", "unable to start receiver module --> " + e.getMessage());
						}
					}
			}
		});
	}
	
	public synchronized List<Message> messageQuery(String publisher, String receiver, long from, long to) {
		Route route = routes.get(publisher); 
		if (to < from || route == null || !route.getReceivers().contains(receiver))
			return null;

		List<Message> result = publishers.get(publisher).messageQuery(from, to);
		return result;
	}
	
	public synchronized void updateRoutes() {
		Map<String, Route> newRoutes = elementManager.getRoutes();
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
		LoggingService.logInfo(MODULE_NAME, "calculating message processing speed");
		System.gc();
		
		long now = System.currentTimeMillis();
		long msgs = StatusReporter.getMessageBusStatus().getProcessedMessages();
		
		float speed = ((msgs * 1.0f) / now) * 1000;
		speed = (speed + StatusReporter.getMessageBusStatus().getAverageSpeed()) / 2.0f;
		StatusReporter.setMessageBusStatus().setAverageSpeed(speed);
	};
	
	private final Runnable checkMessageServerStatus = () -> {
		LoggingService.logInfo(MODULE_NAME, "check message bus server status");
		if (!messageBusServer.isServerActive()) {
			LoggingService.logWarning(MODULE_NAME, "server is not active. restarting...");
			stop();
			try {
				messageBusServer.startServer();
				LoggingService.logInfo(MODULE_NAME, "server restarted");
				init();
			} catch (Exception e) {
				LoggingService.logWarning(MODULE_NAME, "server restart failed --> " + e.getMessage());
			}
		}
		
		if (messageBusServer.isProducerClosed()) {
			LoggingService.logWarning(MODULE_NAME, "producer module stopped. restarting...");
			try {
				messageBusServer.openProducer();
				LoggingService.logInfo(MODULE_NAME, "producer module restarted");
			} catch (Exception e) {
				LoggingService.logWarning(MODULE_NAME, "unable to start producer module --> " + e.getMessage());
			}
		}
		
		receivers.entrySet().forEach(entry -> {
			if (messageBusServer.isConsumerClosed(entry.getKey())) {
				LoggingService.logWarning(MODULE_NAME, "consumer module for " + entry.getKey() + " stopped. restarting...");
				entry.getValue().close();
				try {
					messageBusServer.createCosumer(entry.getKey());
					receivers.put(entry.getKey(), new MessageReceiver(entry.getKey()));
					LoggingService.logInfo(MODULE_NAME, "consumer module restarted");
				} catch (Exception e) {
					LoggingService.logWarning(MODULE_NAME, "unable to restart consumer module for " + entry.getKey() + " --> " + e.getMessage());
				}
			}
		});
	};
	
	public static void update() {
		MessageBus.getInstance().updateRoutes();
	}
	
	public void start() {
		elementManager = ElementManager.getInstance();
//		elementManager.loadFromApi();
		
		messageBusServer = new MessageBusServer();
		try {
			LoggingService.logInfo(MODULE_NAME, "starting message bus server");
			messageBusServer.startServer();
			messageBusServer.initialize();
		} catch (Exception e) {
			try {
				messageBusServer.stopServer();
			} catch (Exception e1) {}
			LoggingService.logWarning(MODULE_NAME, "unable to start message bus server --> " + e.getMessage());
			StatusReporter.setSupervisorStatus().setModuleStatus(Constants.MESSAGE_BUS, ModulesStatus.STOPPED);
		}
		
		LoggingService.logInfo(MODULE_NAME, "starting message bus server");
		init();

		Supervisor.scheduler.scheduleAtFixedRate(calculateSpeed, 0, SPEED_CALCULATION_FREQ_MINUTES, TimeUnit.MINUTES);
		Supervisor.scheduler.scheduleAtFixedRate(checkMessageServerStatus, 5, 5, TimeUnit.SECONDS);
	}
	
	public void stop() {
		for (MessagePublisher publisher : publishers.values())
			publisher.close();
		try {
			messageBusServer.stopServer();
		} catch (Exception e) {}
	}
	
	public static void main(String[] args) throws Exception {
		Configuration.loadConfig();
		try {
			LoggingService.setupLogger();
		} catch (Exception e) {
			System.out.println("Error starting logging service\n" + e.getMessage());
			System.exit(1);
		}
		LoggingService.logInfo("Main", "configuration loaded.");
		MessageBus messageBus = MessageBus.getInstance();
		
		// ********************************************
		
		messageBus.testPublishSimultaneously();
		
		// ********************************************

		messageBus.stop();
		System.exit(0);
	}
	
	
	// tests
	
	private void testPublish() {
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
		for (int i = 0; i < 10001; i++) {
			publishMessage(m);
		}
		System.out.println(System.currentTimeMillis() - start);

		List<Message> messages = getMessages(r);
		for (Message message : messages) {
			System.out.println(message);
		}
	}
	
	private void testDuplicateIds() {
		final int number = 200000;
		Set<String> ids = new HashSet<>();
		System.out.println("STARTED");
		for (int i = 0; i < number; i++) {
			String id = idGenerator.getNextId();
			if (id.equals("") || ids.contains(id))
				System.out.println(id);
			else
				ids.add(id);
		}
		System.out.println("DONE!");
	}
	
	private volatile int threadsCount;
	private void testPublishSimultaneously() throws Exception {
		int maxThreads = 50;
		int messagesPerThread = 5000;
		
		Runnable sendMessage = () -> {
			int delay = new Random().nextInt(20);
			Message m = new Message();
			String p = "DTCnTG4dLyrGC7XYrzzTqNhW7R78hk3V";
			m.setPublisher(p);
			for (int i = 0; i < messagesPerThread; i++) {
				publishMessage(m);
				try {
					Thread.sleep(delay);
				} catch (Exception e) {} 
			}
			synchronized (MessageBus.class) {
				threadsCount++;	
				System.out.println(threadsCount + " DONE!");
			}
		};
		
		long start = System.currentTimeMillis();
		for (int i = 0; i < maxThreads; i++)
			new Thread(sendMessage).start();
		while (threadsCount != maxThreads);
		long stop = System.currentTimeMillis();
		System.out.println(stop - start);
		System.out.println(String.format("%d messages sent ;)", maxThreads * messagesPerThread));

		String r = "wF8VmXTQcyBRPhb27XKgm4gpq97NN2bh";
		getMessages(r);
		System.out.println();
	}
	
}
