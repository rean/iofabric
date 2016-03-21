package com.iotracks.iofabric.supervisor;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.iotracks.iofabric.field_agent.FieldAgent;
import com.iotracks.iofabric.message_bus.Message;
import com.iotracks.iofabric.message_bus.MessageBus;
import com.iotracks.iofabric.process_manager.ProcessManager;
import com.iotracks.iofabric.resource_consumption_manager.ResourceConsumptionManager;
import com.iotracks.iofabric.status_reporter.StatusReporter;
import com.iotracks.iofabric.utils.Constants;
import com.iotracks.iofabric.utils.Constants.ModulesStatus;
import com.iotracks.iofabric.utils.logging.LoggingService;

public class Supervisor {

	private final String MODULE_NAME = "Supervisor";
	public static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
	
	private ProcessManager processManager;
	private ResourceConsumptionManager resourceConsumptionManager;
	private FieldAgent fieldAgent;
	private MessageBus messageBus;

	public Supervisor() {
	}

	public void start() {
		Runtime.getRuntime().addShutdownHook(new Thread(shutdownHook, "shutdown hook"));
		
		LoggingService.logInfo(MODULE_NAME, "starting status reporter");
		StatusReporter.start();
		StatusReporter.setSupervisorStatus().setModuleStatus(Constants.STATUS_REPORTER, ModulesStatus.RUNNING);
		
		StatusReporter.setSupervisorStatus()
				.setDaemonStatus(ModulesStatus.STARTING)
				.setDaemonLastStart(System.currentTimeMillis())
				.setOperationDuration(0);

		// TODO: start other modules
		// TODO: after starting each module, set SupervisorStatus.modulesStatus
		
		// starting Resource Consumption Manager
		LoggingService.logInfo(MODULE_NAME, "starting resource consumption manager");
		StatusReporter.setSupervisorStatus()
				.setModuleStatus(Constants.RESOURCE_CONSUMPTION_MANAGER, ModulesStatus.STARTING);
		resourceConsumptionManager = new ResourceConsumptionManager();
		resourceConsumptionManager.start();
		StatusReporter.setSupervisorStatus()
				.setModuleStatus(Constants.RESOURCE_CONSUMPTION_MANAGER, ModulesStatus.RUNNING);

		// starting Field Agent
		LoggingService.logInfo(MODULE_NAME, "starting field agent");
		StatusReporter.setSupervisorStatus()
				.setModuleStatus(Constants.FIELD_AGENT, ModulesStatus.STARTING);
		fieldAgent = FieldAgent.getInstance();
		fieldAgent.start();
		StatusReporter.setSupervisorStatus()
				.setModuleStatus(Constants.FIELD_AGENT, ModulesStatus.RUNNING);

		// starting Process Manager
		LoggingService.logInfo(MODULE_NAME, "starting process manager");
		StatusReporter.setSupervisorStatus()
				.setModuleStatus(Constants.PROCESS_MANAGER, ModulesStatus.STARTING);
		processManager = new ProcessManager();
		processManager.start();
		StatusReporter.setSupervisorStatus()
				.setModuleStatus(Constants.PROCESS_MANAGER,	ModulesStatus.RUNNING);
		
		// starting Message Bus
		LoggingService.logInfo(MODULE_NAME, "starting message bus");
		StatusReporter.setSupervisorStatus()
				.setModuleStatus(Constants.MESSAGE_BUS, ModulesStatus.STARTING);
		messageBus = MessageBus.getInstance();
		StatusReporter.setSupervisorStatus()
				.setModuleStatus(Constants.MESSAGE_BUS,	ModulesStatus.RUNNING);
		
		
		fieldAgent.addObserver(messageBus);
		fieldAgent.addObserver(processManager);

		StatusReporter.setSupervisorStatus()
				.setDaemonStatus(ModulesStatus.RUNNING);
		LoggingService.logInfo(MODULE_NAME, "started");
		test();
		
		while (true) {
			try {
				Thread.sleep(Constants.STATUS_REPORT_FREQ_SECONDS * 1000);
			} catch (InterruptedException e) {
				LoggingService.logWarning(MODULE_NAME, e.getMessage());
				System.exit(1);
			}
			StatusReporter.setSupervisorStatus()
					.setOperationDuration(System.currentTimeMillis());
		}
	}

	private Thread publisher;
	private Thread receiver;
	@SuppressWarnings("deprecation")
	private final Runnable shutdownHook = () -> {
		try {
			scheduler.shutdownNow();
			publisher.stop();
			receiver.stop();
			messageBus.stop();
		} catch (Exception e) {}
	};

	private void test() {
		int max = Integer.MAX_VALUE;
		Runnable sendMessage = new Runnable() {
			@Override
			public void run() {
				String p = "DTCnTG4dLyrGC7XYrzzTqNhW7R78hk3V";
				Random random = new Random();
				for (int i = 0; i < max; i++) {
					Message m = new Message(p);
					messageBus.publishMessage(m);
					int delay = random.nextInt(5);
					try {
						Thread.sleep(delay);
					} catch (Exception e) {}
				}
				System.out.println("####################### " + max + " messages sent");
			}
		};
		Runnable receiveMessage = new Runnable() {
			@Override
			public void run() {
				int count = 0;
				String r = "wF8VmXTQcyBRPhb27XKgm4gpq97NN2bh";
				Random random = new Random();
				while (count < max) {
					List<Message> messages = messageBus.getMessages(r);
					count += messages.size();
//					if (messages != null)
//						for (Message message : messages)
//							Constants.systemOut.println(message.getTimestamp() + " : " + message.getId());
					int delay = random.nextInt(100);
					try {
						Thread.sleep(delay);
					} catch (Exception e) {}
				}
				System.out.println("$$$$$$$$$$$$$$$$$$$$$$$ " + count + " messages received");
			}
		};
		publisher = new Thread(sendMessage, "message publisher");
		receiver = new Thread(receiveMessage, "message receiver");
		publisher.start();
		receiver.start();
	}
}
