package com.iotracks.iofabric.message_bus;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.iotracks.iofabric.element.ElementManager;
import com.iotracks.iofabric.element.Route;
import com.iotracks.iofabric.utils.configuration.Configuration;

public class MessageBus {
	
	private MessageBusServer messageBusServer;
	private static Map<String, Route> routes;
	private static Map<String, MessagePublisher> publishers;
	private static Map<String, MessageReceiver> receivers;
	private MessageIdGenerator idGenerator;
	
	public MessageBus() {
	}
	
	public Message publishMessage(Message message) {
		long timestamp = System.currentTimeMillis();
		message.setId(idGenerator.generate(timestamp));
		message.setTimestamp(timestamp);
		
		MessagePublisher publisher = publishers.get(message.getPublisher());

		if (publisher != null)
			publisher.publish(message);

		return message;
	}
	
	public List<Message> getMessages(String receiver) {
		if (!receivers.containsKey(receiver))
			return null;
		return receivers.get(receiver).getMessages();
	}
	
	public void enableRealTimeReceiving(String receiver) {
		if (receivers.containsKey(receiver))
			receivers.get(receiver).enableRealTimeReceiving();
	}

	public void disableRealTimeReceiving(String receiver) {
		if (receivers.containsKey(receiver))
			receivers.get(receiver).disableRealTimeReceiving();
	}

	private void updateRoutingTable() {
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
	
	public void updateRoutes() {
		// TODO: receive new changes and update routes, receivers, publishers
	}
	
	public void start() {
		messageBusServer = new MessageBusServer();
		try {
			messageBusServer.startServer();
			messageBusServer.initialize();
		} catch (Exception e) {
			try {
				messageBusServer.stopServer();
			} catch (Exception e1) {
				e1.printStackTrace(System.out);
			}
			e.printStackTrace(System.out);
			System.exit(1);
		}
		
		idGenerator = new MessageIdGenerator();
		publishers = new ConcurrentHashMap<>();
		receivers = new ConcurrentHashMap<>();
		routes = ElementManager.getRoutes();
		updateRoutingTable();

		
		
		
		String p = "DTCnTG4dLyrGC7XYrzzTqNhW7R78hk3V";
		String r = "dFg6jjj4QKKrPGpgfX9FLrzQhFXzYZtc";
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

		for (int i = 0; i < 1100; i++)
			publishMessage(m);

		List<Message> messages = getMessages(r);
		for (Message message : messages) {
			System.out.println(message);
		}
		
		
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
		MessageBus messageBus = new MessageBus();
		messageBus.start();
	}
}
