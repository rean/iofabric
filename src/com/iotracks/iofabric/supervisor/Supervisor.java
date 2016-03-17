package com.iotracks.iofabric.supervisor;

import java.lang.Thread.State;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.iotracks.iofabric.field_agent.FieldAgent;
import com.iotracks.iofabric.message_bus.MessageBus;
import com.iotracks.iofabric.process_manager.ProcessManager;
import com.iotracks.iofabric.resource_consumption_manager.ResourceConsumptionManager;
import com.iotracks.iofabric.status_reporter.StatusReporter;
import com.iotracks.iofabric.utils.Constants;
import com.iotracks.iofabric.utils.Constants.ModulesStatus;
import com.iotracks.iofabric.utils.configuration.Configuration;
import com.iotracks.iofabric.utils.logging.LoggingService;

public class Supervisor {

	private final String MODULE_NAME = "Supervisor";
	private final int CHECK_MODULES_STATUS_FREQ_SECONDS = 2;
	public static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
	
	private Thread processManager;
	private ResourceConsumptionManager resourceConsumptionManager;
	private FieldAgent fieldAgent;
	@SuppressWarnings("unused")
	private MessageBus messageBus;

	public Supervisor() {
	}

	private final Runnable checkStatus = () -> {
		LoggingService.logInfo(MODULE_NAME, "checking modules status");

		if (resourceConsumptionManager.getState() == State.TERMINATED) {
			StatusReporter.setSupervisorStatus().setModuleStatus(Constants.RESOURCE_CONSUMPTION_MANAGER, ModulesStatus.STARTING);
			resourceConsumptionManager = new ResourceConsumptionManager();
			resourceConsumptionManager.start();
		}
		StatusReporter.setSupervisorStatus().setModuleStatus(Constants.RESOURCE_CONSUMPTION_MANAGER, ModulesStatus.RUNNING);


		
		if (processManager.getState() == State.TERMINATED) {
			LoggingService.logWarning(MODULE_NAME, "process manager stopped. restarting...");
			StatusReporter.setSupervisorStatus().setModuleStatus(Constants.PROCESS_MANAGER, ModulesStatus.STARTING);
			processManager = new Thread(new ProcessManager(), "Process Manager");
			processManager.start();
		}
		StatusReporter.setSupervisorStatus().setModuleStatus(Constants.PROCESS_MANAGER, ModulesStatus.RUNNING);
		
		if (Configuration.configChanged) {
			// TODO: Update modules configuration.
			try {
				LoggingService.setupLogger();
			} catch (Exception e) {
				LoggingService.logWarning(MODULE_NAME, "error changing logger config");
			}
			
			Configuration.configChanged = false;
		}
	};
	
	public void start() {
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
		processManager = new Thread(new ProcessManager(), "Process Manager");
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
		
		
		

		// setting up scheduled executor to execute checkStatus
		scheduler.scheduleAtFixedRate(checkStatus, CHECK_MODULES_STATUS_FREQ_SECONDS, CHECK_MODULES_STATUS_FREQ_SECONDS,
				TimeUnit.SECONDS);

		
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

}
