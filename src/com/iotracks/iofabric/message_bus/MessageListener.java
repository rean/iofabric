package com.iotracks.iofabric.message_bus;

import org.hornetq.api.core.client.ClientMessage;
import org.hornetq.api.core.client.MessageHandler;

import com.iotracks.iofabric.local_api.MessageCallback;

public class MessageListener implements MessageHandler{
	private final MessageCallback callback;
	
	public MessageListener(MessageCallback callback) {
		this.callback = callback;
	}
	
	@Override
	public void onMessage(ClientMessage msg) {
		Message message = new Message(msg.getBytesProperty("message"));
		callback.sendRealtimeMessage(message);
	}

}
