package com.iotracks.iofabric.message_bus;

import org.hornetq.api.core.client.ClientMessage;
import org.hornetq.api.core.client.MessageHandler;

public class MessageListener implements MessageHandler{
	private final MessageReceiver receiver;
	
	public MessageListener(MessageReceiver receiver) {
		this.receiver = receiver;
	}
	
	@Override
	public void onMessage(ClientMessage msg) {
		Message message = new Message(msg.getBytesProperty("message"));
		receiver.getCallback().sendRealtimeMessage(message);
	}

}
