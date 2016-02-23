package com.iotracks.iofabric.utils;

import java.io.PrintStream;

public class Constants {
	public enum ModulesStatus {
		STARTING, RUNNING, STOPPED
	}

	public enum DockerStatus {
		NOT_PRESENT, RUNNING, STOPPED
	}

	public enum ElementStatus {
		BUILDING, FAILED_VERIFICATION, STARTING, RUNNING, STOPPED
	}
	
	public enum LinkStatus {
		FAILED_VERIFICATION, FAILED_LOGIN, CONNECTED
	}

	public static final int NUMBER_OF_MODULES = 6;

	public static final int RESOURCE_CONSUMPTION_MANAGER = 0;
	public static final int PROCESS_MANAGER = 1;
	public static final int STATUS_REPORTER = 2;
	public static final int LOCAL_API = 3;
	public static final int MESSAGE_BUS = 4;
	public static final int FIELD_AGENT = 5;

	public static final int STATUS_REPORT_FREQ_SECONDS = 5;

	public static final String MEMORY_MAPPED_FILENAME = "iofabric.params";

	public static PrintStream systemOut;
}
