package com.iotracks.iofabric.utils;

import java.io.PrintStream;

/**
 * holds IOFabric constants
 * 
 * @author saeid
 *
 */
public class Constants {
	public enum ModulesStatus {
		STARTING, RUNNING, STOPPED
	}

	public enum DockerStatus {
		NOT_PRESENT, RUNNING, STOPPED
	}

	public enum ElementState {
		BUILDING, FAILED_VERIFICATION, STARTING, RUNNING, STOPPED 
	}
	
	public enum LinkStatus {
		FAILED_VERIFICATION, FAILED_LOGIN, CONNECTED
	}

	public enum ControllerStatus {
		NOT_PROVISIONED, BROKEN, OK
	}
	
	public static final int NUMBER_OF_MODULES = 6;

	public static final int RESOURCE_CONSUMPTION_MANAGER = 0;
	public static final int PROCESS_MANAGER = 1;
	public static final int STATUS_REPORTER = 2;
	public static final int LOCAL_API = 3;
	public static final int MESSAGE_BUS = 4;
	public static final int FIELD_AGENT = 5;

	public static final int STATUS_REPORT_FREQ_SECONDS = 5;

	public static PrintStream systemOut;

	public static final String address = "iofabric.message_bus";
	public static final String commandlineAddress = "iofabric.commandline";
	
	public static final int KiB = 1024;
	public static final int MiB = 1024 * 1024;
	public static final int GiB = 1024 * 1024 * 1024;
}
