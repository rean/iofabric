package com.iotracks.iofabric.message_bus;

import org.hornetq.api.core.HornetQException;
import org.hornetq.api.core.client.ClientMessage;
import org.hornetq.api.core.client.MessageHandler;

import com.iotracks.iofabric.command_line.CommandLineParser;

public class CommandLineHandler implements MessageHandler {

	@Override
	public void onMessage(ClientMessage message) {
		try {
			message.acknowledge();
		} catch (HornetQException e) {
		}
		String command = message.getStringProperty("command");
		String result = CommandLineParser.parse(command);
		ClientMessage response = MessageBusServer.getSession().createMessage(false);
		response.putStringProperty("response", result);
		response.putObjectProperty("receiver", "iofabric.commandline.response");
		
		try {
			MessageBusServer.getCommandlineProducer().send(response);
		} catch (Exception e) {
		}
	}

}
