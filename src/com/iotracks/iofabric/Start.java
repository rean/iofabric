package com.iotracks.iofabric;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import com.iotracks.iofabric.command_line_communications.CommandLineClient;
import com.iotracks.iofabric.supervisor.Supervisor;
import com.iotracks.iofabric.utils.Constants;
import com.iotracks.iofabric.utils.configuration.Configuration;
import com.iotracks.iofabric.utils.configuration.ConfigurationItemException;
import com.iotracks.iofabric.utils.logging.LoggingService;

public class Start {

	private static Configuration cfg;
	private static LoggingService logger = null;
	private static CommandLineClient client;

	private static boolean isAnotherInstanceRunning() {
		client = new CommandLineClient();
		return client.startClient();
	}

	public static void main(String[] args) {
		try {
			cfg = Configuration.getInstance();
		} catch (ConfigurationItemException e) {
			System.out.println("invalid configuration item(s).");
			System.out.println(e.getMessage());
			System.exit(1);
		} catch (Exception e) {
			System.out.println("error accessing /etc/iofabric/config.xml");
			System.exit(1);
		}

		if (isAnotherInstanceRunning()) {
			if (args.length > 0 && args[0].equals("start")) {
				System.out.println("iofabric is already running.");
				System.exit(1);
			}

			String command = "";
			for (String str : args)
				command += str + " ";
			client.sendMessage(command);
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}

			client.stopClient();
			System.out.println();
			System.exit(0);
		}

		if (args.length > 0 && !args[0].equals("start")) {
			System.out.println("iofabric is not running.");
			System.exit(1);
		}

		try {
			logger = LoggingService.getInstance();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.exit(1);
		}
		logger.log(Level.INFO, "Main", "configuration loaded.");

		// port System.out to null
		Constants.systemOut = System.out;
		System.setOut(new PrintStream(new OutputStream() {
			@Override
			public void write(int b) {
				// DO NOTHING
			}
		}));
		
		System.setErr(new PrintStream(new OutputStream() {
			@Override
			public void write(int b) {
				// DO NOTHING
			}
		}));

		Supervisor supervisor = new Supervisor(cfg, logger);
		supervisor.start();

		// port System.out to standard
		System.setOut(Constants.systemOut);
	}

}
