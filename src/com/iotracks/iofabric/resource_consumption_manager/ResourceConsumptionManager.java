package com.iotracks.iofabric.resource_consumption_manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

import com.iotracks.iofabric.Start;
import com.iotracks.iofabric.status_reporter.StatusReporter;
import com.iotracks.iofabric.supervisor.Supervisor;
import com.iotracks.iofabric.utils.configuration.Configuration;
import com.iotracks.iofabric.utils.logging.LoggingService;

public class ResourceConsumptionManager {
	private final long GET_USAGE_DATA_FREQ_SECONDS = 5;
	private String MODULE_NAME = "Resource Consumption Manager";
	private float diskLimit, cpuLimit, memoryLimit;

	private Runnable getUsageData = () -> {
		LoggingService.logInfo(MODULE_NAME, "get usage data");

		updateConfig();

		float memoryUsage = getMemoryUsage();
		float cpuUsage = getCpuUsage();
		float diskUsage = getDiskUsage();
		StatusReporter.setResourceConsumptionManagerStatus()
				.setMemoryUsage(memoryUsage)
				.setCpuUsage(cpuUsage)
				.setDiskUsage(diskUsage)
				.setMemoryViolation(memoryUsage > memoryLimit)
				.setDiskViolation(diskUsage > diskLimit)
				.setCpuViolation(cpuUsage > cpuLimit);

	};

	public void updateConfig() {
		diskLimit = Configuration.getDiskLimit();
		cpuLimit = Configuration.getCpuLimit();
		memoryLimit = Configuration.getMemoryLimit();
	}

	private float getMemoryUsage() {
		Runtime runtime = Runtime.getRuntime();
		long allocatedMemory = runtime.totalMemory();
		long freeMemory = runtime.freeMemory();
		return (allocatedMemory - freeMemory) / 1024f / 1024f;
	}

	private float getCpuUsage() {
		String processName = ManagementFactory.getRuntimeMXBean().getName();
		String processId = processName.split("@")[0];

		long utimeBefore, utimeAfter, totalBefore, totalAfter;
		float usage = 0;
		try {
			BufferedReader br = new BufferedReader(new FileReader("/proc/" + processId + "/stat"));
			String line = br.readLine();
			utimeBefore = Long.parseLong(line.split(" ")[13]);
			br.close();

			totalBefore = 0;
			br = new BufferedReader(new FileReader("/proc/stat"));
			line = br.readLine();
			while (line != null) {
				String[] items = line.split(" ");
				if (items[0].equals("cpu")) {
					for (int i = 1; i < items.length; i++)
						if (!items[i].trim().equals("") && items[i].matches("[0-9]*"))
							totalBefore += Long.parseLong(items[i]);
					break;
				}
			}
			br.close();

			Thread.sleep(1000);

			br = new BufferedReader(new FileReader("/proc/" + processId + "/stat"));
			line = br.readLine();
			utimeAfter = Long.parseLong(line.split(" ")[13]);
			br.close();

			totalAfter = 0;
			br = new BufferedReader(new FileReader("/proc/stat"));
			line = br.readLine();
			while (line != null) {
				String[] items = line.split(" ");
				if (items[0].equals("cpu")) {
					for (int i = 1; i < items.length; i++)
						if (!items[i].trim().equals("") && items[i].matches("[0-9]*"))
							totalAfter += Long.parseLong(items[i]);
					break;
				}
			}
			br.close();

			usage = 100f * (utimeAfter - utimeBefore) / (totalAfter - totalBefore);
		} catch (Exception e) {
		}
		return usage;
	}

	private long directorySize(File directory) {
		if (!directory.exists())
			return 0;
		if (directory.isFile()) 
			return directory.length();
		long length = 0;
		for (File file : directory.listFiles()) {
			if (file.isFile())
				length += file.length();
			else if (file.isDirectory())
				length += directorySize(file);
		}
		return length;
	}

	private float getDiskUsage() {
		long length = 0;

		length += directorySize(new File(Configuration.getLogDiskDirectory()));
		length += directorySize(new File("/var/run/iofabric"));
		length += directorySize(new File("/var/lib/iofabric"));
		length += directorySize(new File("/etc/iofabric"));
		length += directorySize(new File("/usr/bin/iofabric"));
		length += directorySize(new File("/etc/init.d/iofabric"));

		File self = new File(Start.class.getProtectionDomain().getCodeSource().getLocation().toString().substring(5));
		length += directorySize(self);

		return length / 1024f / 1024f / 1024f;
	}
	
	public void start() {
		Supervisor.scheduler.scheduleAtFixedRate(getUsageData, GET_USAGE_DATA_FREQ_SECONDS, GET_USAGE_DATA_FREQ_SECONDS,
				TimeUnit.SECONDS);

		LoggingService.logInfo(MODULE_NAME, "started");
	}
	
}
