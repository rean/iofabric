package com.iotracks.iofabric.message_bus;

import java.util.HashMap;
import java.util.HashSet;
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
import com.iotracks.iofabric.utils.logging.LoggingService;


public class MessageBusServer {
	
	private final String MODULE_NAME = "Message Bus Server";
	public static final String address = "iofabric.message_bus";
	private ClientSessionFactory sf;
	private HornetQServer server;
	private static ClientSession messageBusSession;
	private ClientConsumer commandlineConsumer;
	private static ClientProducer producer;
	private static Map<String, ClientConsumer> consumers;
	
	protected boolean isServerActive() {
		return server.isActive();
	}
	
	protected boolean isProducerClosed() {
		return producer.isClosed();
	}
	
	protected boolean isConsumerClosed(String name) {
		ClientConsumer consumer = consumers.get(name); 
		return consumer == null || consumer.isClosed();
	}
	
	protected void startServer() throws Exception {
		LoggingService.logInfo(MODULE_NAME, "starting...");
		AddressSettings addressSettings = new AddressSettings();
//		addressSettings.setMaxSizeBytes((long) (Configuration.getMemoryLimit() * 1024 * 1024));
//		addressSettings.setPageSizeBytes((long) (Configuration.getMemoryLimit() * 512 * 1024));
		addressSettings.setMaxSizeBytes(256 * 1024 * 1024);
		addressSettings.setPageSizeBytes(128 * 1024 * 1024);
		addressSettings.setAddressFullMessagePolicy(AddressFullMessagePolicy.PAGE);
		String workingDirectory = Configuration.getDiskDirectory();

        org.hornetq.core.config.Configuration configuration = new ConfigurationImpl();
        configuration.setJournalDirectory(workingDirectory + "messages/journal");
        configuration.setCreateJournalDir(true);
		configuration.setJournalType(JournalType.NIO);
		configuration.setBindingsDirectory(workingDirectory + "messages/binding");
		configuration.setCreateBindingsDir(true);
        configuration.setPersistenceEnabled(true);
        configuration.setSecurityEnabled(false);
        configuration.setPagingDirectory(workingDirectory + "messages/paging");
        configuration.getAddressesSettings().put(address, addressSettings);
//        configuration.setThreadPoolMaxSize(1);
//        configuration.setScheduledThreadPoolMaxSize(1);

		Map<String, Object> connectionParams = new HashMap<>();
		connectionParams.put("port", 55555);
		connectionParams.put("host", "localhost");
//		connectionParams.put("nio-remoting-threads", 3);
		TransportConfiguration nettyConfig = new TransportConfiguration(NettyAcceptorFactory.class.getName(), connectionParams);

        HashSet<TransportConfiguration> transportConfig = new HashSet<>();
		transportConfig.add(nettyConfig);
        transportConfig.add(new TransportConfiguration(InVMAcceptorFactory.class.getName()));
        
		configuration.setAcceptorConfigurations(transportConfig);
		HornetQServer server = HornetQServers.newHornetQServer(configuration);
		server.start();

//        ServerLocator serverLocator = HornetQClient.createServerLocatorWithoutHA(
//        		new TransportConfiguration(NettyConnectorFactory.class.getName(), connectionParams));
        ServerLocator serverLocator = HornetQClient.createServerLocatorWithoutHA(new TransportConfiguration(InVMConnectorFactory.class.getName()));

        sf = serverLocator.createSessionFactory();
	}
	
	protected void initialize() throws Exception {
		messageBusSession = sf.createSession(true, true, 0);
		QueueQuery queueQuery = messageBusSession.queueQuery(new SimpleString(address)); 
		if (queueQuery.isExists())
			messageBusSession.deleteQueue(address);
		messageBusSession.createQueue(address, address, false);
		messageBusSession.close();
		
		messageBusSession = sf.createSession();
		producer = messageBusSession.createProducer(address);
		commandlineConsumer = messageBusSession.createConsumer(address, String.format("receiver = '%s'", "iofabric.commandline.command"));
		commandlineConsumer.setMessageHandler(new CommandLineHandler());
		messageBusSession.start();
	}
	
	protected void createCosumer(String name) throws Exception {
		if (consumers == null)
			consumers = new ConcurrentHashMap<>();

		ClientConsumer consumer = messageBusSession.createConsumer(address, String.format("receiver = '%s'", name));
		consumers.put(name, consumer);
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
		LoggingService.logInfo(MODULE_NAME, "stopping...");
		if (consumers != null)
			consumers.entrySet().forEach(entry -> {
				try {
					entry.getValue().close();
				} catch (Exception e) {	}
			});
		if (producer != null)
			producer.close();
		if (sf != null)
			sf.close();
		if (server != null)
			server.stop();
		LoggingService.logInfo(MODULE_NAME, "starting...");
	}

	protected void openProducer() throws Exception {
		producer = messageBusSession.createProducer(address);
	}
}
