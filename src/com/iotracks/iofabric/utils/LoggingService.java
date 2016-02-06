package com.iotracks.iofabric.utils;

import java.io.File;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingService {
	
	private static LoggingService instance = null;
	private Logger logger;
	
	private LoggingService() throws Exception {
		
		// load configuration
		Configuration cfg = null;
		try {
			cfg = Configuration.getInstance();
		} catch (Exception e) {
			throw new Exception("Error loading configuration \n" + e.getMessage());
		}

		int maxFileSize = (int) (cfg.getLogDiskLimit() * 1024 * 1024); // Convert MiB to bytes
		int logFileCount = cfg.getLogFileCount();
		
		final File logDirectory = new File(cfg.getLogDiskDirectory());
		try {
			logDirectory.mkdirs();
		} catch (Exception e) {
			throw new Exception("Error creating log file directory\n" + e.getMessage());
		}
		
		final String logFilePattern = logDirectory.getPath() + "/" + "iofabric.%g.log"; 
		Handler logFileHandler = new FileHandler(logFilePattern, maxFileSize / logFileCount, logFileCount);
		logFileHandler.setFormatter(new LogFormatter());

		logger = Logger.getLogger("com.iotracks.iofabric");
		logger.addHandler(logFileHandler);
//		logger.setUseParentHandlers(false);
		
		logger.info("logger started.");
	}
	
	
	public static LoggingService getInstance() throws Exception {
		if (instance == null) {
			synchronized (LoggingService.class) {
				if (instance == null) 
					instance = new LoggingService();
			}
		}
		
		return instance;
	}


	public void log(Level level, String moduleName, String msg) {
		logger.log(level, moduleName + " : " + msg);
	}
	
}
