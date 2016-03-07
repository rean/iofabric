package com.iotracks.iofabric.message_bus;

import java.util.ArrayList;
import java.util.List;

import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.core.client.ClientConsumer;
import org.hornetq.api.core.client.ClientMessage;
import org.hornetq.api.core.client.ClientProducer;
import org.hornetq.api.core.client.ClientSession;
import org.hornetq.api.core.client.ClientSessionFactory;
import org.hornetq.api.core.client.HornetQClient;
import org.hornetq.api.core.client.ServerLocator;
import org.hornetq.core.config.Configuration;
import org.hornetq.core.config.impl.ConfigurationImpl;
import org.hornetq.core.remoting.impl.invm.InVMConnectorFactory;
import org.hornetq.core.server.HornetQServer;
import org.hornetq.core.server.HornetQServers;

public class MessageBusUtil {
	
	private final String address = "iofabric";
	private ClientSessionFactory sf;
	private HornetQServer server;
	
	public void startServer() throws Exception {
		Configuration configuration = new ConfigurationImpl();
		// we only need this for the server lock file
		configuration.setJournalDirectory("/var/lib/messages/journal");
		configuration.setPersistenceEnabled(false);
		configuration.setSecurityEnabled(false);
		configuration.setPagingDirectory("/var/lib/messages");
		configuration.getAcceptorConfigurations().add(new TransportConfiguration(InVMConnectorFactory.class.getName()));

		server = HornetQServers.newHornetQServer(configuration);
		server.start();

		ServerLocator serverLocator = HornetQClient
				.createServerLocatorWithoutHA(new TransportConfiguration(InVMConnectorFactory.class.getName()));
		sf = serverLocator.createSessionFactory();
	}
	
	public void createQueue(String queueName) throws Exception {
		ClientSession coreSession = sf.createSession(false, false, false);
		coreSession.createQueue(address, queueName);
		coreSession.close();
	}
	
	public void sendMessage(String recipient, IOMessage message) throws Exception {
		ClientSession session = sf.createSession();
		ClientProducer producer = session.createProducer(recipient);
		ClientMessage msg = session.createMessage(false);
		msg.putObjectProperty("message", message);
	}

	public List<IOMessage> getMessages(String reciever) throws Exception {
		List<IOMessage> result = new ArrayList<>();
		ClientSession session = sf.createSession();
		ClientConsumer consumer = session.createConsumer(reciever);
		ClientMessage msg = consumer.receiveImmediate();
		while (msg != null) {
			result.add((IOMessage) msg.getObjectProperty("message"));
		}
		return result;
	}

	public void stopServer() throws Exception {
		sf.close();
		server.stop();
	}
}
