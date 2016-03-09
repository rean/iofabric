package com.iotracks.iofabric.command_line;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.iotracks.iofabric.field_agent.FieldAgent;
import com.iotracks.iofabric.status_reporter.StatusReporter;
import com.iotracks.iofabric.utils.Constants;
import com.iotracks.iofabric.utils.configuration.Configuration;
import com.iotracks.iofabric.utils.logging.LoggingService;

public class CommandLineParser {
	public static String parse(String command) {
		String[] args = command.split(" ");
		StringBuilder result = new StringBuilder();
		
		if (args[0].equals("stop")) {
			System.setOut(Constants.systemOut);
			System.exit(0);
		}
		
		if (args[0].equals("start")) {
			return "";
		}
		
		if (args[0].equals("help") || args[0].equals("--help") || args[0].equals("-h") || args[0].equals("-?")) {
			result.append(showHelp());
			return result.toString();
		}

		if (args[0].equals("version") || args[0].equals("--version") || args[0].equals("-v")) {
			result.append("ioFabric 1.0");
			result.append("\nCopyright (C) 2016 iotracks, inc.");
			result.append("\nLicense ######### http://iotracks.com/license");
			result.append(
					"\nThis is open-source software with a commercial license: your usage is free until you use it in production commercially.");
			result.append("\nThere is NO WARRANTY, to the extent permitted by law.");

			return result.toString();
		}

		if (args[0].equals("status")) {
			return StatusReporter.getStatusReport();
		}

		if (args[0].equals("deprovision")) {
			result.append("Deprovisioning from controller...");
			result.append("\nSuccess - tokens and identifiers and keys removed");

			return result.toString();
		}

		if (args[0].equals("info")) {
			result.append("Instance ID               : sdfh43t9EFHSD98hwefiuwefkshd890she");
			result.append("\nIP Address                : 201.43.0.88");
			result.append("\nNetwork Adapter           : eth0");
			result.append("\nioFabric Controller       : http://iotracks.com/controllers/2398yef");
			result.append("\nioFabric Certificate      : ~/temp/certs/abc.crt");
			result.append("\nDocker URI                : unix:///var/run/docker.sock");
			result.append("\nDisk Limit                : 14.5 GiB");
			result.append("\nDisk Directory            : ~/temp/spool/");
			result.append("\nMemory Limit              : 720 MiB");
			result.append("\nCPU Limit                 : 74.8%");
			result.append("\nLog Limit                 : 2.0 GiB");
			result.append("\nLog Directory             : ~/temp/logs/");
			result.append("\nLog File Count            : 10");

			return result.toString();
		}

		if (args[0].equals("provision")) {
			if (args.length < 2) {
				return showHelp();
			}
			String provisionKey = args[1];
			result.append("Provisioning with key \"" + provisionKey + "\"...");
			result.append(new FieldAgent().doProvisioning(provisionKey));

			return result.toString();
		}

		if (args[0].equals("config")) {
			if (args.length < 3)
				return showHelp();

			Map<String, String> config = new HashMap<>();
			int i = 1;
			while (i < args.length) {
				if (args.length - i < 2)
					return showHelp();
				if (!args[i].equals("-d") && !args[i].equals("-dl") && !args[i].equals("-m") && !args[i].equals("-p")
						&& !args[i].equals("-a") && !args[i].equals("-ac") && !args[i].equals("-c")
						&& !args[i].equals("-n") && !args[i].equals("-l") && !args[i].equals("-ld") && !args[i].equals("-lc"))
					return showHelp();

				String option = args[i].substring(1);
				String value = args[i + 1];
				config.put(option, value);
				i += 2;
			}
			
			try {
				Configuration.setConfig(config);
				result.append("\nNew configuration");
				for (Entry<String, String> e : config.entrySet())
					result.append("\n\tOption : -" + e.getKey() + "\tValue : " + e.getValue());
			} catch (Exception e) {
				LoggingService.logWarning("Command-line Parser", "error updating new config.");
				result.append("error updating new config.\n" + e.getMessage());
			}
			
			return result.toString();
		}

		return showHelp();
	}

	private static String showHelp() {
		StringBuilder help = new StringBuilder();
		help.append("Usage: iofabric [OPTIONS] COMMAND [arg...]\n");

		help.append("\nOption                   GNU long option              Meaning");
		help.append("\n======                   ===============              =======");
		help.append("\n-h, -?                   --help                       Show this message");
		help.append(
				"\n-v                       --version                    Display the software version and license information\n\n");

		help.append("\nCommand                  Arguments                    Meaning");
		help.append("\n=======                  =========                    =======");
		help.append("\nhelp                                                  Show this message");
		help.append(
				"\nversion                                               Display the software version and license information");
		help.append(
				"\nstatus                                                Display current status information about the software");
		help.append(
				"\nstart                                                 Start the ioFabric daemon which runs in the background");
		help.append("\nstop                                                  Stop the ioFabric daemon");
		help.append("\nrestart                                               Stop and then start the ioFabric daemon");
		help.append(
				"\nprovision                <provisioning key>           Attach this software to the configured ioFabric controller");
		help.append(
				"\ndeprovision                                           Detach this software from all ioFabric controllers");
		help.append(
				"\ninfo                                                  Display the current configuration and other information about the software");
		help.append(
				"\nconfig                   [OPTION] [VALUE]             Change the software configuration according to the options provided");
		help.append(
				"\n                         -d <#GB Limit>               Set the limit, in GiB, of disk space that the software is allowed to use");
		help.append(
				"\n                         -dl <dir>                    Set the directory to use for disk storage");
		help.append(
				"\n                         -m <#MB Limit>               Set the limit, in MiB, of memory that the software is allowed to use");
		help.append(
				"\n                         -p <#cpu % Limit>            Set the limit, in percentage, of CPU time that the software is allowed to use");
		help.append(
				"\n                         -a <uri>                     Set the uri of the fabric controller to which this software connects");
		help.append(
				"\n                         -ac <filepath>               Set the file path of the SSL/TLS certificate for validating the fabric controller identity");
		help.append(
				"\n                         -c <uri>                     Set the UNIX socket or network address that the Docker daemon is using");
		help.append(
				"\n                         -n <network adapter>         Set the name of the network adapter that holds the correct IP address of this machine");
		help.append(
				"\n							-l <#MB Limit>               Set the limit, in MiB, of disk space that the log files can consume");
		help.append(
				"\n							-ld <dir>                    Set the directory to use for log file storage");
		help.append(
				"\n							-lc <#log files>             Set the number of log files to evenly split the log storage limit\n\n");
		
		
		help.append("\nReport bugs to: kilton@iotracks.com");
		help.append("\nioFabric home page: http://iotracks.com");

		return help.toString();
	}

}
