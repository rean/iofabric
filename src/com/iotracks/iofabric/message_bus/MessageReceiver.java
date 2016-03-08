package com.iotracks.iofabric.message_bus;

import java.util.ArrayList;
import java.util.List;

import org.hornetq.api.core.HornetQException;
import org.hornetq.api.core.client.ClientConsumer;
import org.hornetq.api.core.client.ClientMessage;

public class MessageReceiver {
	private final String name;

	private MessageListener listener;
	private ClientConsumer consumer;

	public MessageReceiver(String name) {
		this.name = name;
		consumer = MessageBusServer.getConsumer(name);
	}

	protected List<Message> getMessages() {
		if (consumer == null)
			return null;

		List<Message> result = new ArrayList<>();
		Message message = getMessage();
		while (message != null) {
			result.add(message);
			message = getMessage();
		}
		return result;
	}

	protected Message getMessage() {
		if (consumer == null)
			return null;

		Message result = null; 
		try {
			ClientMessage msg = consumer.receiveImmediate();
			if (msg != null)
				result = new Message(msg.getBytesProperty("message"));
		} catch (HornetQException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(System.out);
		}
		return result;
	}

	protected void update() {
		consumer = MessageBusServer.getConsumer(name);
	}

	protected void setListener() {
		this.listener = new MessageListener(this);
		try {
			consumer.setMessageHandler(listener);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace(System.out);
		}
	}
	
	public String getName() {
		return name;
	}
	
	public void removeListener() {
		try {
			if (listener == null || consumer.getMessageHandler() == null)
				return;
	
			listener = null;
			consumer.setMessageHandler(null);
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}
}
