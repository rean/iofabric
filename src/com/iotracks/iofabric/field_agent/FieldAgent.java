package com.iotracks.iofabric.field_agent;

import java.util.logging.Level;
import com.iotracks.iofabric.utils.Constants;
import com.iotracks.iofabric.utils.ModulesActivity;
import com.iotracks.iofabric.utils.configuration.Configuration;
import com.iotracks.iofabric.utils.logging.LoggingService;

public class FieldAgent implements Runnable {
	
	private final String MODULE_NAME = "Field Agent";
	private ModulesActivity modulesActivity;
	private LoggingService logger;
	@SuppressWarnings("unused")
	private Configuration cfg;
	
	public FieldAgent(LoggingService logger, Configuration cfg) {
		this.logger = logger;
		this.cfg = cfg;
		modulesActivity = ModulesActivity.getInstance();
	}
	
	@Override
	public void run() {
		logger.log(Level.INFO, MODULE_NAME, "started");
		
		// reports it's last active time
		modulesActivity.setModuleLastActiveTime(Constants.FIELD_AGENT);
		
		// to simulate a runtime error (SUICIDE)
		int counter = 0;
		
		while (true) {
			try {
				Thread.sleep(Constants.STATUS_REPORT_FREQ_SECONDS * 1000);
			} catch (Exception e) {
				logger.log(Level.SEVERE, MODULE_NAME, e.getMessage());
			}
			logger.log(Level.INFO, MODULE_NAME, "works well!");
			modulesActivity.setModuleLastActiveTime(Constants.FIELD_AGENT);

			// testing for runtime error!
			try {
				counter++;
				if (counter == 2)
					// oops! divide by zero error...
					System.out.println(1 / 0);
			} catch (Exception e) {
				logger.log(Level.SEVERE, MODULE_NAME, e.getMessage());
				
			}
			
			//testing for infinite loop!
			if (counter == 4)
				while (true);
		}
	}
	
}
