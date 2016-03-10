package com.iotracks.iofabric.message_bus;

import java.util.ArrayList;
import java.util.List;

import org.hornetq.api.core.HornetQException;
import org.hornetq.api.core.client.ClientConsumer;
import org.hornetq.api.core.client.ClientMessage;

import com.iotracks.iofabric.local_api.MessageCallback;

public class MessageReceiver {
	private final String name;

	private MessageListener listener;
	private ClientConsumer consumer;

	public MessageReceiver(String name) {
		this.name = name;
		consumer = MessageBusServer.getConsumer(name);
		listener = null;
	}

	protected List<Message> getMessages() {
		List<Message> result = new ArrayList<>();
		
		if (consumer != null || listener == null) {
			Message message = getMessage();
			while (message != null) {
				result.add(message);
				message = getMessage();
			}
		}
		return result;
	}

	protected Message getMessage() {
		if (consumer == null || listener != null)
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

	protected String getName() {
		return name;
	}
	
	protected void enableRealTimeReceiving() {
		if (consumer == null || consumer.isClosed())
			return;
		listener = new MessageListener(new MessageCallback(name));
		try {
			consumer.setMessageHandler(listener);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace(System.out);
		}
	}
	
	protected void disableRealTimeReceiving() {
		try {
			if (consumer == null || listener == null || consumer.getMessageHandler() == null)
				return;
			listener = null;
			consumer.setMessageHandler(null);
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}
	
	protected void close() {
		if (consumer == null)
			return;
		disableRealTimeReceiving();
		try {
			consumer.close();
		} catch (HornetQException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(System.out);
		}
	}
}
