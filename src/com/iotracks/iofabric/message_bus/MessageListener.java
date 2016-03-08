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
		//TODO: send message to local api
		
		Message message = new Message(msg.getBytesProperty("message"));
		System.out.println("message received by " + receiver.getName() + "\n\t" + message.toString());
	}

}
