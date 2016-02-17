package com.iotracks.iofabric.utils.logging;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.iotracks.iofabric.utils.configuration.Configuration;

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

		int maxFileSize = (int) (cfg.getLogDiskLimit() * 1024 * 1024); // Convert
																		// MiB
																		// to
																		// bytes
		int logFileCount = cfg.getLogFileCount();

		final File logDirectory = new File(cfg.getLogDiskDirectory());
		try {
			logDirectory.mkdirs();
		} catch (Exception e) {
			throw new Exception("Error creating log file directory\n" + e.getMessage());
		}

		Files.setPosixFilePermissions(logDirectory.toPath(),
				EnumSet.of(PosixFilePermission.GROUP_EXECUTE, PosixFilePermission.GROUP_READ,
						PosixFilePermission.GROUP_WRITE, PosixFilePermission.OWNER_EXECUTE,
						PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
		
		final String logFilePattern = logDirectory.getPath() + "/iofabric.%g.log";
		Handler logFileHandler = null;
		try {
			logFileHandler = new FileHandler(logFilePattern, maxFileSize / logFileCount, logFileCount);
		} catch (SecurityException | IOException e) {
			throw new Exception("Error creating log file\n" + e.getMessage());
		}
		logFileHandler.setFormatter(new LogFormatter());

		logger = Logger.getLogger("com.iotracks.iofabric");
		logger.addHandler(logFileHandler);
		logger.setUseParentHandlers(false);
		
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
