package com.iotracks.iofabric.supervisor;

import java.lang.Thread.State;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.iotracks.iofabric.field_agent.FieldAgent;
import com.iotracks.iofabric.local_api.LocalApi;
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
	private Thread localApiThread;
	
	private Runnable checkLocalApiStatus = () -> {
		if (localApiThread != null && localApiThread.getState() == State.TERMINATED) {
			localApiThread = new Thread(LocalApi.getInstance(), "Local Api");
			localApiThread.start();
		}
	};

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
		
		LocalApi localApi = LocalApi.getInstance();
		localApiThread = new Thread(localApi, "Local Api");
		localApiThread.start();
		scheduler.scheduleAtFixedRate(checkLocalApiStatus, 0, 10, TimeUnit.SECONDS);

		fieldAgent.addObserver(messageBus);
		fieldAgent.addObserver(processManager);
		fieldAgent.addObserver(localApi);

		StatusReporter.setSupervisorStatus()
				.setDaemonStatus(ModulesStatus.RUNNING);
		LoggingService.logInfo(MODULE_NAME, "started");
		
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

	private final Runnable shutdownHook = () -> {
		try {
			scheduler.shutdownNow();
			messageBus.stop();
		} catch (Exception e) {}
	};

}
