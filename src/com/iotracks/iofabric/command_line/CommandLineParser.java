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
			try {
				result.append(FieldAgent.getInstance().deProvision());
			} catch (Exception e) {}

			return result.toString();
		}

		if (args[0].equals("info")) {
			result.append(Configuration.getConfigReport());

			return result.toString();
		}

		if (args[0].equals("provision")) {
			if (args.length < 2) {
				return showHelp();
			}
			String provisionKey = args[1];
			result.append("Provisioning with key \"" + provisionKey + "\"...");
			result.append(FieldAgent.getInstance().provision(provisionKey));

			return result.toString();
		}

		if (args[0].equals("config")) {
			if (args.length < 3)
				return showHelp();

			Map<String, Object> config = new HashMap<>();
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
				result.append("Change(s) accepted");
				for (Entry<String, Object> e : config.entrySet())
					result.append("\n\tParameter : -").append(e.getKey()).append("\tValue : ").append(e.getValue().toString());
			} catch (Exception e) {
				LoggingService.logWarning("Command-line Parser", "error updating new config.");
				result.append("error updating new config : " + e.getMessage());
			}
			
			return result.toString();
		}

		return showHelp();
	}

	private static String showHelp() {
		StringBuilder help = new StringBuilder();
		help.append("Usage: iofabric [OPTIONS] COMMAND [arg...]\n" + 
				"\n" + 
				"Option           GNU long option         Meaning\n" + 
				"======           ===============         =======\n" + 
				"-h, -?           --help                  Show this message\n" + 
				"-v               --version               Display the software version and\n" + 
				"                                         license information\n" + 
				"\n" + 
				"\n" + 
				"Command          Arguments               Meaning\n" + 
				"=======          =========               =======\n" + 
				"help                                     Show this message\n" + 
				"version                                  Display the software version and\n" + 
				"                                         license information\n" + 
				"status                                   Display current status information\n" + 
				"                                         about the software\n" + 
				"start                                    Start the ioFabric daemon which\n" + 
				"                                         runs in the background\n" + 
				"stop                                     Stop the ioFabric daemon\n" + 
				"restart                                  Stop and then start the ioFabric\n" + 
				"                                         daemon\n" + 
				"provision        <provisioning key>      Attach this software to the\n" + 
				"                                         configured ioFabric controller\n" + 
				"deprovision                              Detach this software from all\n" + 
				"                                         ioFabric controllers\n" + 
				"info                                     Display the current configuration\n" + 
				"                                         and other information about the\n" + 
				"                                         software\n" + 
				"config           [OPTION] [VALUE]        Change the software configuration\n" + 
				"                                         according to the options provided\n" + 
				"                 -d <#GB Limit>          Set the limit, in GiB, of disk space\n" + 
				"                                         that the software is allowed to use\n" + 
				"                 -dl <dir>               Set the directory to use for disk\n" + 
				"                                         storage\n" + 
				"                 -m <#MB Limit>          Set the limit, in MiB, of memory that\n" + 
				"                                         the software is allowed to use\n" + 
				"                 -p <#cpu % Limit>       Set the limit, in percentage, of CPU\n" + 
				"                                         time that the software is allowed\n" + 
				"                                         to use\n" + 
				"                 -a <uri>                Set the uri of the fabric controller\n" + 
				"                                         to which this software connects\n" + 
				"                 -ac <filepath>          Set the file path of the SSL/TLS\n" + 
				"                                         certificate for validating the fabric\n" + 
				"                                         controller identity\n" + 
				"                 -c <uri>                Set the UNIX socket or network address\n" + 
				"                                         that the Docker daemon is using\n" + 
				"                 -n <network adapter>    Set the name of the network adapter\n" + 
				"                                         that holds the correct IP address of \n" + 
				"                                         this machine\n" + 
				"                 -l <#MB Limit>          Set the limit, in MiB, of disk space\n" + 
				"                                         that the log files can consume\n" + 
				"                 -ld <dir>               Set the directory to use for log file\n" + 
				"                                         storage\n" + 
				"                 -lc <#log files>        Set the number of log files to evenly\n" + 
				"                                         split the log storage limit\n" + 
				"\n" + 
				"\n" + 
				"Report bugs to: kilton@iotracks.com\n" + 
				"ioFabric home page: http://iotracks.com");
		
		return help.toString();
	}

}
