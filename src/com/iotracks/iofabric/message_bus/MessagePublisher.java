package com.iotracks.iofabric.message_bus;

import org.hornetq.api.core.client.ClientMessage;
import org.hornetq.api.core.client.ClientProducer;
import org.hornetq.api.core.client.ClientSession;

import com.iotracks.iofabric.element.Route;

public class MessagePublisher {
	private final String name;
	private final MessageArchive archive;
	
	private ClientProducer producer;
	private ClientSession session;
	private Route route;
	
	public MessagePublisher(String name, Route route) {
		this.name = name;
		this.archive = new MessageArchive(name);
		this.route = route;
		producer = MessageBusServer.getProducer();
		session = MessageBusServer.getSession();
	}
	
	protected void publish(Message message) {
		byte[] bytes;
		try {
			bytes = message.getBytes();
		} catch (Exception e) {
			e.printStackTrace(System.out);
			return;
		}

		archive.save(bytes, message.getTimestamp());
		for (String receiver : route.getReceivers()) {
			ClientMessage msg = session.createMessage(true);
			msg.putObjectProperty("receiver", receiver);
			try {
				msg.putBytesProperty("message", bytes);
				producer.send(msg);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace(System.out);
			}
		}
	}
	
	protected void update() {
		session = MessageBusServer.getSession();
		producer = MessageBusServer.getProducer();
	}
	
	protected void updateRoute(Route route) {
		this.route = route;
	}

	public void close() {
		try {
			archive.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace(System.out);
		}
	}
}
