package com.iotracks.iofabric.status_reporter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.iotracks.iofabric.field_agent.FieldAgentStatus;
import com.iotracks.iofabric.local_api.LocalApiStatus;
import com.iotracks.iofabric.message_bus.MessageBusStatus;
import com.iotracks.iofabric.process_manager.ProcessManagerStatus;
import com.iotracks.iofabric.resource_consumption_manager.ResourceConsumptionManagerStatus;
import com.iotracks.iofabric.supervisor.SupervisorStatus;
import com.iotracks.iofabric.utils.Constants.ControllerStatus;
import com.iotracks.iofabric.utils.logging.LoggingService;

/**
 * Status Reporter module
 * 
 * @author saeid
 *
 */
public final class StatusReporter {
	
	private static int SET_SYSTEM_TIME_FREQ_SECONDS = 60;
	private static SupervisorStatus supervisorStatus = new SupervisorStatus();
	private static ResourceConsumptionManagerStatus resourceConsumptionManagerStatus = new ResourceConsumptionManagerStatus();
	private static FieldAgentStatus fieldAgentStatus = new FieldAgentStatus();
	private static StatusReporterStatus statusReporterStatus = new StatusReporterStatus();
	private static ProcessManagerStatus processManagerStatus = new ProcessManagerStatus();
	private static LocalApiStatus localApiStatus = new LocalApiStatus();
	private static MessageBusStatus messageBusStatus = new MessageBusStatus();
	
	private static String MODULE_NAME = "Status Reporter";
	
	/**
	 * sets system time property
	 * 
	 */
	private static Runnable setStatusReporterSystemTime = () -> {
		try {
			setStatusReporterStatus().setSystemTime(System.currentTimeMillis());
		} catch (Exception e) {}
	};
	
	private StatusReporter() {
	}

	/**
	 * returns report for "status" command-line parameter
	 * 
	 * @return status report
	 */
	public static String getStatusReport() {
		StringBuilder result = new StringBuilder();
		
		final DateFormat df = new SimpleDateFormat("MMM dd yyyy hh:mm:ss.SSS");
		
		float diskUsage = resourceConsumptionManagerStatus.getDiskUsage();
		String connectionStatus = fieldAgentStatus.getContollerStatus() == ControllerStatus.OK ? "ok" : 
			(fieldAgentStatus.getContollerStatus() == ControllerStatus.BROKEN ? "broken" : "not provisioned"); 
		result.append("ioFabric daemon             : " + supervisorStatus.getDaemonStatus().name());
		result.append("\nMemory Usage                : about " + String.format("%.2f", resourceConsumptionManagerStatus.getMemoryUsage()) + " MiB");
		if (diskUsage < 1)
			result.append("\nDisk Usage                  : about " + String.format("%.2f", diskUsage * 1024) + " MiB");
		else
			result.append("\nDisk Usage                  : about " + String.format("%.2f", diskUsage) + " GiB");
		result.append("\nCPU Usage                   : about " + String.format("%.2f", resourceConsumptionManagerStatus.getCpuUsage()) + "%");
		result.append("\nRunning Elements            : " + processManagerStatus.getRunningElementsCount());
		result.append("\nConnection to Controller    : " + connectionStatus);
		result.append(String.format("\nMessages Processed          : about %,d", messageBusStatus.getProcessedMessages()));  
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

	public static MessageBusStatus setMessageBusStatus() {
		statusReporterStatus.setLastUpdate(System.currentTimeMillis());
		return messageBusStatus;
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
	
	public static ProcessManagerStatus getProcessManagerStatus() {
		return processManagerStatus;
	}
	
	public static LocalApiStatus setLocalApiStatus() {
		statusReporterStatus.setLastUpdate(System.currentTimeMillis());
		return localApiStatus;
	}

	public static SupervisorStatus getSupervisorStatus() {
		return supervisorStatus;
	}
	
	public static MessageBusStatus getMessageBusStatus() {
		return messageBusStatus;
	}

	public static ResourceConsumptionManagerStatus getResourceConsumptionManagerStatus() {
		return resourceConsumptionManagerStatus;
	}

	public static FieldAgentStatus getFieldAgentStatus() {
		return fieldAgentStatus;
	}

	public static StatusReporterStatus getStatusReporterStatus() {
		return statusReporterStatus;
	}

	public static LocalApiStatus getLocalApiStatus() {
		return localApiStatus;
	}

	/**
	 * starts Status Reporter module
	 * 
	 */
	public static void start() {
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(setStatusReporterSystemTime, 0, SET_SYSTEM_TIME_FREQ_SECONDS, TimeUnit.SECONDS);
		LoggingService.logInfo(MODULE_NAME, "started");
	}

}
