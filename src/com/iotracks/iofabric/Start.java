package com.iotracks.iofabric;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.logging.Level;
import com.iotracks.iofabric.supervisor.Supervisor;
import com.iotracks.iofabric.utils.Constants;
import com.iotracks.iofabric.utils.MemoryMappedFileService;
import com.iotracks.iofabric.utils.configuration.Configuration;
import com.iotracks.iofabric.utils.configuration.ConfigurationItemException;
import com.iotracks.iofabric.utils.logging.LoggingService;

public class Start {

	private static File lockFile;
	private static FileChannel channel;
	private static FileLock lock;
	private static Configuration cfg;
	private static LoggingService logger = null;

	@SuppressWarnings("resource")
	private static boolean isAnotherInstanceRunning() {
		final File dir = new File(cfg.getLogDiskDirectory());
		try {
			dir.mkdirs();
		} catch (Exception e) {
			System.out.println("Error creating system files directory");
			System.exit(1);
		}
		cfg.setLogDiskDirectory(dir.getPath());

		lockFile = new File(dir.getPath() + "/iofabric.lck");
		// Try to get the lock
		try {
			channel = new RandomAccessFile(lockFile, "rw").getChannel();
		} catch (FileNotFoundException e) {
			System.out.println("unable to create lock file");
			System.exit(1);
		}
		try {
			lock = channel.tryLock();
		} catch (IOException e) {
			System.out.println("unable to lock the file");
			System.exit(1);
		}

		if (lock == null) {
			// File is locked by other instance
			try {
				channel.close();
			} catch (IOException e) {
			}
			return true;
		}

		// release the lock before shutdown
		Thread shutdownHook = new Thread(new Runnable() {
			@Override
			public void run() {
				logger.log(Level.INFO, "Main", "shutting down!");
				if (lock != null) {
					try {
						lock.release();
						channel.close();
						lockFile.delete();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		Runtime.getRuntime().addShutdownHook(shutdownHook);

		return false;
	}

	private static void sendCommands(String[] args) {
		String commands = "";
		MemoryMappedFileService fileService = new MemoryMappedFileService();
		for (String str : args)
			commands += str + " ";

//		System.out.println("SEND...");
		try {
			fileService.sendString(cfg.getLogDiskDirectory() + "/" + Constants.MEMORY_MAPPED_FILENAME, commands.trim());
		} catch (Exception e) {
			e.printStackTrace();
		}

//		System.out.println("RECEIVE...");
		String result = "";
		try {
			while (result.trim().equals("")) {
				Thread.sleep(100);
				result = fileService.getString(cfg.getLogDiskDirectory() + "/" + Constants.MEMORY_MAPPED_FILENAME);
			}
			System.out.println(result.trim());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {
			cfg = Configuration.getInstance();
		} catch (ConfigurationItemException e) {
			System.out.println("invalid configuration item(s).");
			System.out.println(e.getMessage());
			System.exit(1);
		} catch (Exception e) {
			System.out.println("error accessing config/config.xml");
			System.exit(1);
		}

		if (isAnotherInstanceRunning()) {
			if (args.length > 0) {
				sendCommands(args);
			} else {
				System.out.println("iofabric is already running");
			}
			System.exit(0);
		}

		try {
			logger = LoggingService.getInstance();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.exit(1);
		}
		logger.log(Level.INFO, "Main", "configuration loaded.");

		Supervisor supervisor = new Supervisor(cfg, logger);
		supervisor.start();

	}

}
