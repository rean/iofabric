package com.iotracks.iofabric.message_bus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.hornetq.api.core.HornetQException;
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
	public static final String commandlineAddress = "iofabric.commandline";
	private ClientSessionFactory sf;
	private HornetQServer server;
	private static ClientSession messageBusSession;
	private ClientConsumer commandlineConsumer;
	private static ClientProducer commandlineProducer;
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
		long memoryLimit = (long) (Configuration.getMemoryLimit() * 1_000_000);
		addressSettings.setMaxSizeBytes(memoryLimit);
		addressSettings.setAddressFullMessagePolicy(AddressFullMessagePolicy.DROP);
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
        
		Map<String, Object> connectionParams = new HashMap<>();
		connectionParams.put("port", 55555);
		connectionParams.put("host", "localhost");
		TransportConfiguration nettyConfig = new TransportConfiguration(NettyAcceptorFactory.class.getName(), connectionParams);

        HashSet<TransportConfiguration> transportConfig = new HashSet<>();
		transportConfig.add(nettyConfig);
        transportConfig.add(new TransportConfiguration(InVMAcceptorFactory.class.getName()));
        
		configuration.setAcceptorConfigurations(transportConfig);
		server = HornetQServers.newHornetQServer(configuration);
		server.start();

        ServerLocator serverLocator = HornetQClient.createServerLocatorWithoutHA(new TransportConfiguration(InVMConnectorFactory.class.getName()));

        serverLocator.setUseGlobalPools(false);
        serverLocator.setScheduledThreadPoolMaxSize(10);
        serverLocator.setThreadPoolMaxSize(10);
        sf = serverLocator.createSessionFactory();
	}
	
	protected void initialize() throws Exception {
		messageBusSession = sf.createSession(true, true, 0);
		QueueQuery queueQuery = messageBusSession.queueQuery(new SimpleString(address));
		if (queueQuery.isExists())
			messageBusSession.deleteQueue(address);
		queueQuery = messageBusSession.queueQuery(new SimpleString(commandlineAddress));
		if (queueQuery.isExists())
			messageBusSession.deleteQueue(commandlineAddress);
		messageBusSession.createQueue(address, address, false);
		messageBusSession.createQueue(commandlineAddress, commandlineAddress, false);

		producer = messageBusSession.createProducer(address);
		commandlineProducer = messageBusSession.createProducer(commandlineAddress);
		
		commandlineConsumer = messageBusSession.createConsumer(commandlineAddress, String.format("receiver = '%s'", "iofabric.commandline.command"));
		commandlineConsumer.setMessageHandler(new CommandLineHandler());
		messageBusSession.start();

		Runnable countMessages = new Runnable() {
			@Override
			public void run() {
				try {
					QueueQuery queueQuery = messageBusSession.queueQuery(new SimpleString(address));
					LoggingService.logInfo(MODULE_NAME, String.valueOf(queueQuery.getMessageCount()));
				} catch (HornetQException e) {
				}
			}
		};
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
		scheduler.scheduleAtFixedRate(countMessages, 10, 10, TimeUnit.SECONDS);
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
	
	public static ClientProducer getCommandlineProducer() {
		return commandlineProducer;
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
		if (commandlineConsumer != null)
			commandlineConsumer.close();
		if (producer != null)
			producer.close();
		if (sf != null)
			sf.close();
		if (server != null)
			server.stop();
		LoggingService.logInfo(MODULE_NAME, "stopped");
	}

	protected void openProducer() throws Exception {
		producer = messageBusSession.createProducer(address);
	}

	public void setMemoryLimit() {
		AddressSettings addressSettings = new AddressSettings();
		long memoryLimit = (long) (Configuration.getMemoryLimit() * 1_000_000);
		addressSettings.setMaxSizeBytes(memoryLimit);
		addressSettings.setAddressFullMessagePolicy(AddressFullMessagePolicy.DROP);

		server.getAddressSettingsRepository().addMatch(address, addressSettings);
	}
}
