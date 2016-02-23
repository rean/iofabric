package com.iotracks.iofabric.status_reporter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.iotracks.iofabric.field_agent.FieldAgentStatus;
import com.iotracks.iofabric.local_api.LocalApiStatus;
import com.iotracks.iofabric.process_manager.ProcessManagerStatus;
import com.iotracks.iofabric.resource_consumption_manager.ResourceConsumptionManagerStatus;
import com.iotracks.iofabric.supervisor.SupervisorStatus;
import com.iotracks.iofabric.utils.logging.LoggingService;

public final class StatusReporter {
	
	private static SupervisorStatus supervisorStatus = new SupervisorStatus();
	private static ResourceConsumptionManagerStatus resourceConsumptionManagerStatus = new ResourceConsumptionManagerStatus();
	private static FieldAgentStatus fieldAgentStatus = new FieldAgentStatus();
	private static StatusReporterStatus statusReporterStatus = new StatusReporterStatus();
	private static ProcessManagerStatus processManagerStatus = new ProcessManagerStatus();
	private static LocalApiStatus localApiStatus = new LocalApiStatus();
	
	private static String MODULE_NAME = "Status Reporter";
	
	// set status reporter's system time every 1 minute
	private static Runnable setStatusReporterSystemTime = () -> {
		setStatusReporterStatus().setSystemTime(System.currentTimeMillis());
	};
	
	private StatusReporter() {
	}

	public static String getStatusReport() {
		StringBuilder result = new StringBuilder();
		
		final DateFormat df = new SimpleDateFormat("MMM dd yyyy hh:mm:ss.SSS");
		
		float diskUsage = resourceConsumptionManagerStatus.getDiskUsage();
		
		result.append("ioFabric daemon             : " + supervisorStatus.getDaemonStatus().name());
		result.append("\nMemory Usage                : about " + String.format("%.2f", resourceConsumptionManagerStatus.getMemoryUsage()) + " MiB");
		if (diskUsage < 1)
			result.append("\nDisk Usage                  : about " + String.format("%.2f", diskUsage * 1024) + " MiB");
		else
			result.append("\nDisk Usage                  : about " + String.format("%.2f", diskUsage) + " GiB");
		result.append("\nCPU Usage                   : about " + String.format("%.2f", resourceConsumptionManagerStatus.getCpuUsage()) + "%");
		result.append("\nRunning Elements            : " + processManagerStatus.getRunningElementsCount());
		result.append("\nConnection to Controller    : [ok][broken][not provisioned]");				// TODO : get from field agent
		result.append("\nMessages Processed          : about 1,583,323"); 							// TODO : get from message bus 
		result.append("\nSystem Time                 : " + df.format(statusReporterStatus.getSystemTime()));
		
		return result.toString();
	}
	
	public static SupervisorStatus setSupervisorStatus() {
		statusReporterStatus.setLastUpdate(System.currentTimeMillis());
		return supervisorStatus;
	}

	public static ResourceConsumptionManagerStatus setResourceConsumptionManagerStatus() {
		statusReporterStatus.setLastUpdate(System.currentTimeMillis());
		return resourceConsumptionManagerStatus;
	}

	public static FieldAgentStatus setFieldAgentStatus() {
		statusReporterStatus.setLastUpdate(System.currentTimeMillis());
		return fieldAgentStatus;
	}

	public static StatusReporterStatus setStatusReporterStatus() {
		statusReporterStatus.setLastUpdate(System.currentTimeMillis());
		return statusReporterStatus;
	}
	
	public static ProcessManagerStatus setProcessManagerStatus() {
		statusReporterStatus.setLastUpdate(System.currentTimeMillis());
		return processManagerStatus;
	}
	
	public static LocalApiStatus setLocalApiStatus() {
		statusReporterStatus.setLastUpdate(System.currentTimeMillis());
		return localApiStatus;
	}

	public static void start() {
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(setStatusReporterSystemTime, 0, 60, TimeUnit.SECONDS);
		LoggingService.log(Level.INFO, MODULE_NAME, "started");
	}

}
