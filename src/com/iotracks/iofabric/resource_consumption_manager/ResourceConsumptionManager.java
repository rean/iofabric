package com.iotracks.iofabric.resource_consumption_manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

import org.apache.commons.compress.utils.ArchiveUtils;

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
		float logUsage = directorySize(Configuration.getLogDiskDirectory());
		float archiveUsage = directorySize(Configuration.getDiskDirectory() + "messages/archive/");
		float diskUsage = logUsage + archiveUsage;
		
		StatusReporter.setResourceConsumptionManagerStatus()
				.setMemoryUsage(memoryUsage / 1024 / 1024)
				.setCpuUsage(cpuUsage)
				.setDiskUsage(diskUsage / 1024 / 1024 / 1024)
				.setMemoryViolation(memoryUsage > memoryLimit)
				.setDiskViolation(diskUsage > diskLimit)
				.setCpuViolation(cpuUsage > cpuLimit);
		
		if (diskUsage > diskLimit) {
			float amount = diskUsage - diskLimit;
			float logViolation = (amount / diskUsage) * logUsage;
			float archiveViolation = (amount / diskUsage) * archiveUsage;
			removeLogFiles(logViolation);
			removeArchives(archiveViolation);
		}

	};

	public void updateConfig() {
		diskLimit = Configuration.getDiskLimit() * 1024 * 1024 * 1024;
		cpuLimit = Configuration.getCpuLimit();
		memoryLimit = Configuration.getMemoryLimit() * 1024 * 1024;
	}

	private void removeArchives(float archiveViolation) {
		String archivesDirectory = Configuration.getDiskDirectory() + "messages/archive/";
		
		final File workingDirectory = new File(archivesDirectory);
		File[] filesList = workingDirectory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String fileName) {
				return fileName.substring(fileName.indexOf(".")).equals(".idx");
			}
		});
		
		Arrays.sort(filesList, new Comparator<File>() {
			public int compare(File o1, File o2) {
				String t1 = o1.getName().substring(o1.getName().indexOf('_') + 1, o1.getName().indexOf("."));
				String t2 = o2.getName().substring(o2.getName().indexOf('_') + 1, o2.getName().indexOf("."));
				return t1.compareTo(t2);
			}
		});
		
		for (File indexFile : filesList) {
			File dataFile = new File(archivesDirectory + indexFile.getName().substring(0, indexFile.getName().indexOf('.')) + ".iomsg");
			archiveViolation -= indexFile.length();
			indexFile.delete();
			archiveViolation -= dataFile.length();
			dataFile.delete();
			if (archiveViolation < 0)
				break;
		}
	}
	
	private void removeLogFiles(float logViolation) {
		String logsDirectory = Configuration.getLogDiskDirectory();
		
		final File workingDirectory = new File(logsDirectory);
		File[] filesList = workingDirectory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String fileName) {
				String str = fileName.substring(fileName.indexOf('.') + 1);
				return str.substring(str.indexOf(".")).equals(".log");
			}
		});
		
		Arrays.sort(filesList, new Comparator<File>() {
			public int compare(File o1, File o2) {
				return o2.getName().compareTo(o1.getName());
			}
		});
		
		for (File logFile : filesList) {
			logViolation -= logFile.length();
			logFile.delete();
			if (logViolation < 0)
				break;
		}
	}

	private float getMemoryUsage() {
		Runtime runtime = Runtime.getRuntime();
		long allocatedMemory = runtime.totalMemory();
		long freeMemory = runtime.freeMemory();
		return (allocatedMemory - freeMemory);
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
		} catch (Exception e) {}
		return usage;
	}

	private long directorySize(String name) {
		File directory = new File(name);
		if (!directory.exists())
			return 0;
		if (directory.isFile()) 
			return directory.length();
		long length = 0;
		for (File file : directory.listFiles()) {
			if (file.isFile())
				length += file.length();
			else if (file.isDirectory())
				length += directorySize(file.getPath());
		}
		return length;
	}

	public void start() {
		Supervisor.scheduler.scheduleAtFixedRate(getUsageData, GET_USAGE_DATA_FREQ_SECONDS, GET_USAGE_DATA_FREQ_SECONDS,
				TimeUnit.SECONDS);

		LoggingService.logInfo(MODULE_NAME, "started");
	}
	
}
