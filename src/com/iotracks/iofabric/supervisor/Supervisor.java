package com.iotracks.iofabric.supervisor;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.iotracks.iofabric.resource_consumption_manager.ResourceConsumptionManager;
import com.iotracks.iofabric.status_reporter.StatusReporter;
import com.iotracks.iofabric.supervisor.SupervisorStatus.StatusEnum;
import com.iotracks.iofabric.utils.Constants;
import com.iotracks.iofabric.utils.logging.LoggingService;

public class Supervisor {

	private final String MODULE_NAME = "Supervisor";
	private final int CHECK_MODULES_STATUS_FREQ_SECONDS = 2;

	public Supervisor() {
	}

	private final Runnable checkStatus = () -> {
		LoggingService.log(Level.INFO, MODULE_NAME, "checking modules status");
	};

	public void start() {
		LoggingService.log(Level.INFO, MODULE_NAME, "starting status reporter");
		StatusReporter.start();
		StatusReporter.setSupervisorStatus().setModuleStatus(Constants.STATUS_REPORTER, StatusEnum.running);
		
		StatusReporter.setSupervisorStatus().setDaemonStatus(StatusEnum.starting)
				.setDaemonLastStart(System.currentTimeMillis()).setOperationDuration(0);

		// setting up scheduled executor to execute checkStatus
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(checkStatus, CHECK_MODULES_STATUS_FREQ_SECONDS, CHECK_MODULES_STATUS_FREQ_SECONDS,
				TimeUnit.SECONDS);

		// TODO: start other modules
		// TODO: after starting each module, set SupervisorStatus.modulesStatus
		LoggingService.log(Level.INFO, MODULE_NAME, "starting resource consumption manager");
		StatusReporter.setSupervisorStatus().setModuleStatus(Constants.RESOURCE_CONSUMPTION_MANAGER,
				StatusEnum.starting);
		ResourceConsumptionManager resourceConsumptionManager = new ResourceConsumptionManager();
		resourceConsumptionManager.start();
		StatusReporter.setSupervisorStatus().setModuleStatus(Constants.RESOURCE_CONSUMPTION_MANAGER,
				StatusEnum.running);

		StatusReporter.setSupervisorStatus().setDaemonStatus(StatusEnum.running);
		LoggingService.log(Level.INFO, MODULE_NAME, "started");
		while (true) {
			try {
				Thread.sleep(Constants.STATUS_REPORT_FREQ_SECONDS * 1000);
			} catch (InterruptedException e) {
				LoggingService.log(Level.SEVERE, MODULE_NAME, e.getMessage());
				System.exit(1);
			}
			StatusReporter.setSupervisorStatus().setOperationDuration(System.currentTimeMillis());
		}
	}

}
