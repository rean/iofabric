package com.iotracks.iofabric.message_bus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.iotracks.iofabric.element.Element;
import com.iotracks.iofabric.element.ElementManager;
import com.iotracks.iofabric.element.Route;
import com.iotracks.iofabric.status_reporter.StatusReporter;
import com.iotracks.iofabric.utils.Constants;
import com.iotracks.iofabric.utils.Constants.ModulesStatus;
import com.iotracks.iofabric.utils.configuration.Configuration;
import com.iotracks.iofabric.utils.logging.LoggingService;

/**
 * Message Bus module
 * 
 * @author saeid
 *
 */
public class MessageBus {
	
	private final String MODULE_NAME = "Message Bus";
	private final int SPEED_CALCULATION_FREQ_MINUTES = 1;
	
	private MessageBusServer messageBusServer;
	private Map<String, Route> routes;
	private Map<String, MessagePublisher> publishers;
	private Map<String, MessageReceiver> receivers;
	private MessageIdGenerator idGenerator;
	private static MessageBus instance;
	private ElementManager elementManager;
	private Object updateLock = new Object();
	
	private long lastSpeedTime, lastSpeedMessageCount;
	
	private MessageBus() {
	}
	
	public static MessageBus getInstance() {
		if (instance == null) {
			synchronized (MessageBus.class) {
				if (instance == null) { 
					instance = new MessageBus();
					instance.start();
				}
			}
		}
		return instance;
	}
	
	
	/**
	 * sets messageId and timestamp and publish the {@link Message}
	 * 
	 * @param message - {@link Message} to be published
	 * @return published {@link Message} containing the id and timestamp 
	 */
	public Message publishMessage(Message message) {
		long timestamp = System.currentTimeMillis();
		StatusReporter.setMessageBusStatus().increasePublishedMessagesPerElement(message.getPublisher());
		message.setId(idGenerator.getNextId());
		message.setTimestamp(timestamp);
		
		MessagePublisher publisher = publishers.get(message.getPublisher());

		if (publisher != null)
			try {
				publisher.publish(message);
			} catch (Exception e) {
				LoggingService.logWarning(MODULE_NAME + "(" + message.getPublisher() + ")", "unable to send message --> " + e.getMessage());
			}

		return message;
	}
	
	/**
	 * gets list of {@link Message} for receiver
	 * 
	 * @param receiver - ID of {@link Element}
	 * @return list of {@link Message}
	 */
	public List<Message> getMessages(String receiver) {
		MessageReceiver rec = receivers.get(receiver); 
		if (rec == null)
			return null;
		List<Message> messages = new ArrayList<>();
		try {
			messages = rec.getMessages();
		} catch (Exception e) {
			LoggingService.logWarning(MODULE_NAME + "(" + receiver + ")", "unable to receive messages --> " + e.getMessage());
		} 
		return messages;
	}
	
	/**
	 * enables real-time {@link Message} receiving of an {@link Element} 
	 * 
	 * @param receiver - ID of {@link Element}
	 */
	public void enableRealTimeReceiving(String receiver) {
		MessageReceiver rec = receivers.get(receiver); 
		if (rec == null)
			return;
		rec.enableRealTimeReceiving();
	}

	/**
	 * disables real-time {@link Message} receiving of an {@link Element} 
	 * 
	 * @param receiver - ID of {@link Element}
	 */
	public void disableRealTimeReceiving(String receiver) {
		MessageReceiver rec = receivers.get(receiver); 
		if (rec == null)
			return;
		rec.disableRealTimeReceiving();
	}

	/**
	 * initialize list of {@link Message} publishers and receivers
	 * 
	 */
	private void init() {
		lastSpeedMessageCount = 0;
		lastSpeedTime = System.currentTimeMillis();
		
		routes = elementManager.getRoutes();
		idGenerator = new MessageIdGenerator();
		publishers = new ConcurrentHashMap<>();
		receivers = new ConcurrentHashMap<>();

		if (routes == null)
			return;
		
		routes.entrySet().stream()
			.filter(route -> route.getValue() != null)
			.filter(route -> route.getValue().getReceivers() != null)
			.forEach(entry -> {
					publishers.put(entry.getKey(), new MessagePublisher(entry.getKey(), entry.getValue()));
					receivers.putAll(entry.getValue().getReceivers()
							.stream()
							.filter(item -> !receivers.containsKey(item))
							.collect(Collectors.toMap(item -> item, item -> {
								try {
									messageBusServer.createCosumer(item);
								} catch (Exception e) {
									LoggingService.logWarning(MODULE_NAME + "(" + item + ")",
											"unable to start receiver module --> " + e.getMessage());
								}
								return new MessageReceiver(item, messageBusServer.getConsumer(item));
							})));
			});

	}
	
	/**
	 * gets list of {@link Message} within a time frame
	 * 
	 * @param publisher - ID of {@link Element}
	 * @param receiver - ID of {@link Element}
	 * @param from - beginning of time frame
	 * @param to - end of time frame
	 * @return list of {@link Message}
	 */
	public synchronized List<Message> messageQuery(String publisher, String receiver, long from, long to) {
		Route route = routes.get(publisher); 
		if (to < from || route == null || !route.getReceivers().contains(receiver))
			return null;

		MessagePublisher messagePublisher = publishers.get(publisher);
		if (messagePublisher == null)
			return null;
		return messagePublisher.messageQuery(from, to);
	}
	
	/**
	 * calculates the average speed of {@link Message} moving through ioFabric
	 * 
	 */
	private final Runnable calculateSpeed = () -> {
		try {
			LoggingService.logInfo(MODULE_NAME, "calculating message processing speed");

			long now = System.currentTimeMillis();
			long msgs = StatusReporter.getMessageBusStatus().getProcessedMessages();

			float speed = ((float)(msgs - lastSpeedMessageCount)) / (now - lastSpeedTime);
			StatusReporter.setMessageBusStatus().setAverageSpeed(speed);
			lastSpeedMessageCount = msgs;
			lastSpeedTime = now;
		} catch (Exception e) {}
	};
	
	/**
	 * monitors HornetQ server
	 * 
	 */
	private final Runnable checkMessageServerStatus = () -> {
		try {
			LoggingService.logInfo(MODULE_NAME, "check message bus server status");
			if (!messageBusServer.isServerActive()) {
				LoggingService.logWarning(MODULE_NAME, "server is not active. restarting...");
				stop();
				try {
					messageBusServer.startServer();
					LoggingService.logInfo(MODULE_NAME, "server restarted");
					init();
				} catch (Exception e) {
					LoggingService.logWarning(MODULE_NAME, "server restart failed --> " + e.getMessage());
				}
			}
			
			if (messageBusServer.isProducerClosed()) {
				LoggingService.logWarning(MODULE_NAME, "producer module stopped. restarting...");
				try {
					messageBusServer.openProducer();
					LoggingService.logInfo(MODULE_NAME, "producer module restarted");
				} catch (Exception e) {
					LoggingService.logWarning(MODULE_NAME, "unable to start producer module --> " + e.getMessage());
				}
			}
			
			receivers.entrySet().forEach(entry -> {
				if (messageBusServer.isConsumerClosed(entry.getKey())) {
					LoggingService.logWarning(MODULE_NAME, "consumer module for " + entry.getKey() + " stopped. restarting...");
					entry.getValue().close();
					try {
						messageBusServer.createCosumer(entry.getKey());
						receivers.put(entry.getKey(), new MessageReceiver(entry.getKey(), messageBusServer.getConsumer(entry.getKey())));
						LoggingService.logInfo(MODULE_NAME, "consumer module restarted");
					} catch (Exception e) {
						LoggingService.logWarning(MODULE_NAME, "unable to restart consumer module for " + entry.getKey() + " --> " + e.getMessage());
					}
				}
			});
		} catch (Exception e) {}
	};
	
	/**
	 * updates routing, list of publishers and receivers
	 * Field Agent calls this method when any changes applied
	 * 
	 */
	public void update() {
		synchronized (updateLock) {
			Map<String, Route> newRoutes = elementManager.getRoutes();
			List<String> newPublishers = new ArrayList<>();
			List<String> newReceivers = new ArrayList<>();
			
			if (newRoutes != null) {
				newRoutes.entrySet()
					.stream()
					.filter(route -> route.getValue() != null)
					.filter(route -> route.getValue().getReceivers() != null)
					.forEach(entry -> {
						newPublishers.add(entry.getKey());
						newReceivers.addAll(entry.getValue().getReceivers()
								.stream().filter(item -> !newReceivers.contains(item))
								.collect(Collectors.toList()));
					});
			}

			publishers.entrySet().forEach(entry -> {
				if (!newPublishers.contains(entry.getKey()))
					entry.getValue().close();
			});
			publishers.entrySet().removeIf(entry -> !newPublishers.contains(entry.getKey()));
			publishers.putAll(
					newPublishers.stream()
					.filter(publisher -> !publishers.containsKey(publisher))
					.collect(Collectors.toMap(publisher -> publisher, 
							publisher -> new MessagePublisher(publisher, newRoutes.get(publisher)))));

			receivers.entrySet().forEach(entry -> {
				if (!newReceivers.contains(entry.getKey())) {
					entry.getValue().close();
					messageBusServer.removeConsumer(entry.getKey());
				}
			});
			receivers.entrySet().removeIf(entry -> !newReceivers.contains(entry.getKey()));
			receivers.putAll(
					newReceivers.stream()
					.filter(receiver -> !receivers.containsKey(receiver))
					.collect(Collectors.toMap(receiver -> receiver, 
							receiver -> new MessageReceiver(receiver, messageBusServer.getConsumer(receiver)))));

			routes = newRoutes;

			StatusReporter.getMessageBusStatus()
				.getPublishedMessagesPerElement().entrySet().removeIf(entry -> {
					return !elementManager.elementExists(entry.getKey());
				});
			elementManager.getElements().forEach(e -> {
				if (!StatusReporter.getMessageBusStatus().getPublishedMessagesPerElement().entrySet().contains(e.getElementId()))
						StatusReporter.getMessageBusStatus().getPublishedMessagesPerElement().put(e.getElementId(), 0l);
			});
		}
	}
	
	/**
	 * sets  memory usage limit of HornetQ
	 * {@link Configuration} calls this method when any changes applied
	 * 
	 */
	public void instanceConfigUpdated() {
		messageBusServer.setMemoryLimit();
	}
	
	/**
	 * starts Message Bus module
	 * 
	 */
	public void start() {
		elementManager = ElementManager.getInstance();
		
		messageBusServer = new MessageBusServer();
		try {
			LoggingService.logInfo(MODULE_NAME, "STARTING MESSAGE BUS SERVER");
			messageBusServer.startServer();
			messageBusServer.initialize();
		} catch (Exception e) {
			try {
				messageBusServer.stopServer();
			} catch (Exception e1) {}
			LoggingService.logWarning(MODULE_NAME, "unable to start message bus server --> " + e.getMessage());
			StatusReporter.setSupervisorStatus().setModuleStatus(Constants.MESSAGE_BUS, ModulesStatus.STOPPED);
		}
		
		LoggingService.logInfo(MODULE_NAME, "MESSAGE BUS SERVER STARTED");
		init();

		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(calculateSpeed, 0, SPEED_CALCULATION_FREQ_MINUTES, TimeUnit.MINUTES);
		scheduler.scheduleAtFixedRate(checkMessageServerStatus, 5, 5, TimeUnit.SECONDS);
	}
	
	/**
	 * closes receivers and publishers and stops HornetQ server
	 * 
	 */
	public void stop() {
		for (MessageReceiver receiver : receivers.values()) 
			receiver.close();
		
		for (MessagePublisher publisher : publishers.values())
			publisher.close();
		try {
			messageBusServer.stopServer();
		} catch (Exception e) {}
	}
}
