package com.iotracks.iofabric.utils.logging;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.iotracks.iofabric.utils.configuration.Configuration;

public final class LoggingService {

	private static Logger logger;

	private LoggingService() {

	}

	public static void log(Level level, String moduleName, String msg) {
		logger.log(level, moduleName + " : " + msg);
	}

	public static void setupLogger() {
		int maxFileSize = (int) (Configuration.getLogDiskLimit() * 1024 * 1024); // Convert
																		// MiB
																		// to
																		// bytes
		int logFileCount = Configuration.getLogFileCount();

		final File logDirectory = new File(Configuration.getLogDiskDirectory());
		try {
			logDirectory.mkdirs();

			UserPrincipalLookupService lookupservice = FileSystems.getDefault().getUserPrincipalLookupService();
			final GroupPrincipal group = lookupservice.lookupPrincipalByGroupName("iofabric");
			Files.getFileAttributeView(logDirectory.toPath(), PosixFileAttributeView.class,
					LinkOption.NOFOLLOW_LINKS).setGroup(group);
			Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwxrwx---");
			Files.setPosixFilePermissions(logDirectory.toPath(), perms);

			final String logFilePattern = logDirectory.getPath() + "/iofabric.%g.log";
			Handler logFileHandler = null;
			logFileHandler = new FileHandler(logFilePattern, maxFileSize / logFileCount, logFileCount);

			logFileHandler.setFormatter(new LogFormatter());

			logger = Logger.getLogger("com.iotracks.iofabric");
			logger.addHandler(logFileHandler);

			logger.setUseParentHandlers(false);

			logger.info("logger started.");
		} catch (Exception e) {
			System.out.println("Error starting logging service\n" + e.getMessage());
			System.exit(1);
		}
	}

}
