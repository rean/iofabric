package com.iotracks.iofabric.message_bus;

import java.util.List;

import org.hornetq.api.core.client.ClientMessage;
import org.hornetq.api.core.client.ClientProducer;
import org.hornetq.api.core.client.ClientSession;

import com.iotracks.iofabric.element.Route;
import com.iotracks.iofabric.utils.logging.LoggingService;

public class MessagePublisher {
	private final MessageArchive archive;
	
	private final String name;
	private ClientProducer producer;
	private ClientSession session;
	private Route route;
	private Object lock = new Object();
	
	public MessagePublisher(String name, Route route) {
		this.archive = new MessageArchive(name);
		this.route = route;
		this.name = name;
		producer = MessageBusServer.getProducer();
		session = MessageBusServer.getSession();
	}
	
	protected void publish(Message message) throws Exception {
		synchronized (lock) {
			byte[] bytes = message.getBytes();

			try {
				archive.save(bytes, message.getTimestamp());
			} catch (Exception e) {
				LoggingService.logWarning("Message Publisher (" + this.name + ")", "unable to archive massage --> " + e.getMessage());
			}
			for (String receiver : route.getReceivers()) {
				ClientMessage msg = session.createMessage(false);
				msg.putObjectProperty("receiver", receiver);
				msg.putBytesProperty("message", bytes);
				producer.send(msg);
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
		synchronized (lock) {
			try {
				archive.close();
			} catch (Exception e) {}
		}
	}

	public List<Message> messageQuery(long from, long to) {
		return archive.messageQuery(from, to);
	}
}
