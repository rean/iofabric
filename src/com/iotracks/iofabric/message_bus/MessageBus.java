package com.iotracks.iofabric.message_bus;

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
	
	public Message publishMessage(Message message) throws Exception {
		long timestamp = System.currentTimeMillis();
		message.setId(idGenerator.generate(timestamp));
		message.setTimestamp(timestamp);
		
		MessagePublisher publisher = publishers.get(message.getPublisher());

		if (publisher != null)
			publisher.publish(message);

		return message;
	}
	
	public void enableRealTimeReceiving(String receiver) {
		receivers.get(receiver).setListener();
	}

	public void disableRealTimeReceiving(String receiver) {
		receivers.get(receiver).removeListener();
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
