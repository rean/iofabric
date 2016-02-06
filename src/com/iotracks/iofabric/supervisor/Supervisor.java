package com.iotracks.iofabric.supervisor;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import com.iotracks.iofabric.field_agent.FieldAgent;
import com.iotracks.iofabric.utils.Constants;
import com.iotracks.iofabric.utils.LoggingService;
import com.iotracks.iofabric.utils.ModulesActivity;



public class Supervisor implements Runnable {
	
	private final String MODULE_NAME = "Supervisor";
	private final int CHECK_MODULES_STATUS_FREQ_SECONDS = 2;
	private final int DEAD_MODULE_AGE_SECONDS = 10;
	
	private Thread[] modulesThread;
	private LoggingService logger;
	private ModulesActivity modulesActivity;
	
	// checks other modules status
	// restarts modules if finds them dead!
	private final Runnable checkStatus = () -> {
		logger.log(Level.INFO, MODULE_NAME, "checking modules status");
		
		long now = new Date().getTime();
		if (Math.abs(now - modulesActivity.getModuleLastAvtiveTime(Constants.FIELD_AGENT)) > DEAD_MODULE_AGE_SECONDS * 1000) {
			logger.log(Level.WARNING, MODULE_NAME, "field_agent module is dead");
			if (modulesThread[Constants.FIELD_AGENT].isAlive()) {
				logger.log(Level.WARNING, MODULE_NAME, "interrupting field_agent");
				modulesThread[Constants.FIELD_AGENT].interrupt();
			}
			logger.log(Level.WARNING, MODULE_NAME, "re-starting field_agent");
			modulesThread[Constants.FIELD_AGENT] = new Thread(new FieldAgent());
			modulesThread[Constants.FIELD_AGENT].start();
		}
	};
	
	
	@Override
	public void run() {
		try {
			logger = LoggingService.getInstance();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		logger.log(Level.INFO, MODULE_NAME, "started");

		modulesActivity = ModulesActivity.getInstance();
		
		// defining an array for modules.
		// it's pretty easy to add new modules in the future!
		// just increase Constants.NUMBER_OF_MODULES and set Coonstants.NEW_MODULE_CODE!
		modulesThread = new Thread[Constants.NUMBER_OF_MODULES];

		// starting Field Agent module
		logger.log(Level.INFO, MODULE_NAME, "starting field_agent");
		modulesThread[Constants.FIELD_AGENT] = new Thread(new FieldAgent());
		modulesThread[Constants.FIELD_AGENT].start();
		
		
		// setting up scheduled executor to execute checkStatus method
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
