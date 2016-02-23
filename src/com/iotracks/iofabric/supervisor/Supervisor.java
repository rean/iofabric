package com.iotracks.iofabric.supervisor;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

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

	public Supervisor() {
	}

	private final Runnable checkStatus = () -> {
		LoggingService.log(Level.INFO, MODULE_NAME, "checking modules status");
		if (Configuration.configChanged) {
			// TODO: Update modules configuration.
			try {
				LoggingService.setupLogger();
			} catch (Exception e) {
				LoggingService.log(Level.WARNING, MODULE_NAME, "error changing logger config");
			}
			
			Configuration.configChanged = false;
		}
	};
	
	public void start() {
		LoggingService.log(Level.INFO, MODULE_NAME, "starting status reporter");
		StatusReporter.start();
		StatusReporter.setSupervisorStatus().setModuleStatus(Constants.STATUS_REPORTER, ModulesStatus.RUNNING);
		
		StatusReporter.setSupervisorStatus()
				.setDaemonStatus(ModulesStatus.STARTING)
				.setDaemonLastStart(System.currentTimeMillis())
				.setOperationDuration(0);

		// setting up scheduled executor to execute checkStatus
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(checkStatus, CHECK_MODULES_STATUS_FREQ_SECONDS, CHECK_MODULES_STATUS_FREQ_SECONDS,
				TimeUnit.SECONDS);

		// TODO: start other modules
		// TODO: after starting each module, set SupervisorStatus.modulesStatus
		
		// starting Resource Consumption Manager
		LoggingService.log(Level.INFO, MODULE_NAME, "starting resource consumption manager");
		StatusReporter.setSupervisorStatus()
				.setModuleStatus(Constants.RESOURCE_CONSUMPTION_MANAGER, ModulesStatus.STARTING);
		ResourceConsumptionManager resourceConsumptionManager = new ResourceConsumptionManager();
		resourceConsumptionManager.start();
		StatusReporter.setSupervisorStatus()
				.setModuleStatus(Constants.RESOURCE_CONSUMPTION_MANAGER, ModulesStatus.RUNNING);

		// starting Process Manager
		LoggingService.log(Level.INFO, MODULE_NAME, "starting process manager");
		StatusReporter.setSupervisorStatus()
				.setModuleStatus(Constants.PROCESS_MANAGER, ModulesStatus.STARTING);
		ProcessManager processManager = new ProcessManager();
		processManager.start();
		StatusReporter.setSupervisorStatus()
				.setModuleStatus(Constants.PROCESS_MANAGER,	ModulesStatus.RUNNING);

		StatusReporter.setSupervisorStatus()
				.setDaemonStatus(ModulesStatus.RUNNING);
		LoggingService.log(Level.INFO, MODULE_NAME, "started");
		while (true) {
			try {
				Thread.sleep(Constants.STATUS_REPORT_FREQ_SECONDS * 1000);
			} catch (InterruptedException e) {
				LoggingService.log(Level.SEVERE, MODULE_NAME, e.getMessage());
				System.exit(1);
			}
			StatusReporter.setSupervisorStatus()
					.setOperationDuration(System.currentTimeMillis());
		}
	}

}
