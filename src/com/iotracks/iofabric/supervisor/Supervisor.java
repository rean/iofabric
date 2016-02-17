package com.iotracks.iofabric.supervisor;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.iotracks.iofabric.command_line_communications.CommandLineServer;
import com.iotracks.iofabric.field_agent.FieldAgent;
import com.iotracks.iofabric.utils.Constants;
import com.iotracks.iofabric.utils.ModulesActivity;
import com.iotracks.iofabric.utils.configuration.Configuration;
import com.iotracks.iofabric.utils.logging.LoggingService;

public class Supervisor {

	private final String MODULE_NAME = "Supervisor";
	private final int CHECK_MODULES_STATUS_FREQ_SECONDS = 2;
	private final int DEAD_MODULE_AGE_SECONDS = 10;

	private Thread fieldAgent;
	private LoggingService logger;
	private Configuration cfg;
	private ModulesActivity modulesActivity;

	public Supervisor(Configuration cfg, LoggingService logger) {
		this.cfg = cfg;
		this.logger = logger;
	}

	// checks other modules status
	// restarts modules if finds them dead!
	private final Runnable checkStatus = () -> {
		logger.log(Level.INFO, MODULE_NAME, "checking modules status");

		long now = System.currentTimeMillis();
		if (Math.abs(now - modulesActivity.getModuleLastActiveTime(Constants.FIELD_AGENT)) > DEAD_MODULE_AGE_SECONDS
				* 1000) {
			logger.log(Level.WARNING, MODULE_NAME, "Field Agent module is dead");
			if (fieldAgent.isAlive()) {
				logger.log(Level.WARNING, MODULE_NAME, "interrupting Field Agent");
				fieldAgent.interrupt();
			}
			fieldAgent.interrupt();
			logger.log(Level.WARNING, MODULE_NAME, "re-starting Field Agent");
			fieldAgent.start();
		}

	};

	public void start() {
		logger.log(Level.INFO, MODULE_NAME, "started");

		modulesActivity = ModulesActivity.getInstance();

		// starting Field Agent module
		logger.log(Level.INFO, MODULE_NAME, "starting Field Agent");
		fieldAgent = new Thread(new FieldAgent(logger, cfg), "Field Agent");
		fieldAgent.start();

		(new Thread(new CommandLineServer(logger), "Command Line Server")).start();
		
		// setting up scheduled executor to execute checkStatus and
		// checkForNewConfig methods
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(checkStatus, 0, CHECK_MODULES_STATUS_FREQ_SECONDS, TimeUnit.SECONDS);

		while (true) {
			try {
				Thread.sleep(Constants.STATUS_REPORT_FREQ_SECONDS * 1000);
			} catch (InterruptedException e) {
				logger.log(Level.SEVERE, MODULE_NAME, e.getMessage());
				System.exit(1);
			}
			logger.log(Level.INFO, MODULE_NAME, "is doing his job!");
		}
	}

}
