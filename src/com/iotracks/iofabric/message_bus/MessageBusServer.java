package com.iotracks.iofabric.message_bus;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hornetq.api.core.SimpleString;
import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.core.client.ClientConsumer;
import org.hornetq.api.core.client.ClientProducer;
import org.hornetq.api.core.client.ClientSession;
import org.hornetq.api.core.client.ClientSession.QueueQuery;
import org.hornetq.api.core.client.ClientSessionFactory;
import org.hornetq.api.core.client.HornetQClient;
import org.hornetq.api.core.client.ServerLocator;
import org.hornetq.core.config.Configuration;
import org.hornetq.core.config.impl.ConfigurationImpl;
import org.hornetq.core.remoting.impl.invm.InVMAcceptorFactory;
import org.hornetq.core.remoting.impl.invm.InVMConnectorFactory;
import org.hornetq.core.server.HornetQServer;
import org.hornetq.core.server.HornetQServers;
import org.hornetq.core.server.JournalType;
import org.hornetq.core.settings.impl.AddressFullMessagePolicy;
import org.hornetq.core.settings.impl.AddressSettings;

public class MessageBusServer {
	
	private final String address = "iofabric.message_bus";
	private ClientSessionFactory sf;
	private HornetQServer server;
	private static ClientSession messageBusSession;
	private static ClientProducer producer;
	private static Map<String, ClientConsumer> consumers;
	
	protected void startServer() throws Exception {
		AddressSettings addressSettings = new AddressSettings();
		addressSettings.setMaxSizeBytes(500 * 1024);
		addressSettings.setPageSizeBytes(100 * 1024);
		addressSettings.setAddressFullMessagePolicy(AddressFullMessagePolicy.PAGE);
		Map<String, AddressSettings> s = new HashMap<>();
		s.put(address, addressSettings);

		Configuration configuration = new ConfigurationImpl();
        configuration.setJournalDirectory("/var/lib/iofabric/messages/journal");
        configuration.setCreateJournalDir(true);
        configuration.setJournalType(JournalType.NIO);
        configuration.setBindingsDirectory("/var/log/iofabric/messages/binding");
        configuration.setCreateBindingsDir(true);
        configuration.setPagingDirectory("/var/lib/iofabric/messages/paging");
        configuration.setPersistenceEnabled(true);
        configuration.setSecurityEnabled(false);
        configuration.setAddressesSettings(s);
        configuration.getAcceptorConfigurations().add(new TransportConfiguration(InVMAcceptorFactory.class.getName()));

        server = HornetQServers.newHornetQServer(configuration);
        server.start();

        ServerLocator serverLocator = HornetQClient.createServerLocatorWithoutHA(new TransportConfiguration(InVMConnectorFactory.class.getName()));
        sf = serverLocator.createSessionFactory();
	}
	
	protected void initialize() throws Exception {
		messageBusSession = sf.createSession(true, true);

		QueueQuery queueQuery = messageBusSession.queueQuery(new SimpleString(address)); 
		if (!queueQuery.isExists())
			messageBusSession.createQueue(address, address, true);
		
		producer = messageBusSession.createProducer(address);
		
		messageBusSession.start();
	}
	
	protected void createCosumer(String name) {
		if (consumers == null)
			consumers = new ConcurrentHashMap<>();

		try {
			ClientConsumer consumer = messageBusSession.createConsumer(address, String.format("receiver = '%s'", name));
			consumers.put(name, consumer);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace(System.out);
		}
	}
	
	protected static ClientSession getSession() {
		return messageBusSession;
	}
	
	protected static ClientProducer getProducer() {
		return producer;
	}
	
	protected static ClientConsumer getConsumer(String receiver) {
		if (consumers == null)
			return null;
		return consumers.get(receiver);
	}
	
	protected void stopServer() throws Exception {
		producer.close();
		consumers.entrySet().forEach(entry -> {
			try {
				entry.getValue().close();
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
		});
		sf.close();
		server.stop();
	}
}
