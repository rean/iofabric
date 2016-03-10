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
import org.hornetq.core.config.impl.ConfigurationImpl;
import org.hornetq.core.remoting.impl.invm.InVMAcceptorFactory;
import org.hornetq.core.remoting.impl.invm.InVMConnectorFactory;
import org.hornetq.core.remoting.impl.netty.NettyAcceptorFactory;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.core.server.HornetQServer;
import org.hornetq.core.server.HornetQServers;
import org.hornetq.core.server.JournalType;
import org.hornetq.core.settings.impl.AddressFullMessagePolicy;
import org.hornetq.core.settings.impl.AddressSettings;

import com.iotracks.iofabric.utils.configuration.Configuration;


public class MessageBusServer {
	
	private final String address = "iofabric.message_bus";
	private ClientSessionFactory sf;
	private HornetQServer server;
	private static ClientSession messageBusSession;
	private static ClientProducer producer;
	private static Map<String, ClientConsumer> consumers;
	
	protected void startServer() throws Exception {
		Map<String, Object> connectionParams = new HashMap<String, Object>();
		connectionParams.put("port", 5445);
		connectionParams.put("host", "localhost");

		TransportConfiguration transportConfiguration = 
		    new TransportConfiguration(
		    NettyAcceptorFactory.class.getName(), connectionParams);

		AddressSettings addressSettings = new AddressSettings();
		
		addressSettings.setMaxSizeBytes((long) (Configuration.getMemoryLimit() * 1024 * 1024));
		addressSettings.setPageSizeBytes((long) (Configuration.getMemoryLimit() * 512 * 1024));
		addressSettings.setAddressFullMessagePolicy(AddressFullMessagePolicy.PAGE);
		String workingDirectory = Configuration.getDiskDirectory();

		org.hornetq.core.config.Configuration configuration = new ConfigurationImpl();
        configuration.setJournalDirectory(workingDirectory + "messages/journal");
        configuration.setCreateJournalDir(true);
        configuration.setJournalType(JournalType.NIO);
        configuration.setBindingsDirectory(workingDirectory + "messages/binding");
        configuration.setCreateBindingsDir(true);
        configuration.setPagingDirectory(workingDirectory + "messages/paging");
        configuration.setPersistenceEnabled(false);
        configuration.setSecurityEnabled(false);
        configuration.getAddressesSettings().put(address, addressSettings);
//        configuration.getAcceptorConfigurations().add(transportConfiguration);
        configuration.getAcceptorConfigurations().add(new TransportConfiguration(InVMAcceptorFactory.class.getName()));

        server = HornetQServers.newHornetQServer(configuration);
        server.start();

        ServerLocator serverLocator = HornetQClient.createServerLocatorWithoutHA(new TransportConfiguration(InVMConnectorFactory.class.getName()));
//        ServerLocator serverLocator = HornetQClient.createServerLocatorWithoutHA(new TransportConfiguration(NettyConnectorFactory.class.getName()));
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
		if (producer != null)
			producer.close();
		if (consumers != null)
			consumers.entrySet().forEach(entry -> {
				try {
					entry.getValue().close();
				} catch (Exception e) {
					e.printStackTrace(System.out);
				}
			});
		if (sf != null)
			sf.close();
		if (server != null)
			server.stop();
	}
}
