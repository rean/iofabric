package com.iotracks.iofabric.field_agent;

import java.util.HashMap;
import java.util.Map;

import javax.json.JsonObject;

import com.iotracks.iofabric.element.ElementManager;
import com.iotracks.iofabric.status_reporter.StatusReporter;
import com.iotracks.iofabric.utils.Orchestrator;
import com.iotracks.iofabric.utils.configuration.Configuration;
import com.iotracks.iofabric.utils.logging.LoggingService;

public class FieldAgent {
	private final String MODULE_NAME = "Field Agent";

	private Orchestrator orchestrator = new Orchestrator();
	private long lastGetChangesList;

	public FieldAgent() {
		lastGetChangesList = 0;
	}

	private final Runnable loadElements = () -> {
		while (true) {
			getChangesList();
			try {
				Thread.sleep(10000);
			} catch (Exception e) {
			}
		}
	};

	public void start() {
		new Thread(loadElements, MODULE_NAME).start();
	}

	private void sendStatus() {
		Map<String, Object> postParams = new HashMap<>();

		// elementstatus - JSON string providing the status of all elements
		// (example below)
		// [{"id":"sdfkjhweSDDkjhwer8","status":"starting","starttime":1234567890123,"operatingduration":278421},{"id":"239y7dsDSFuihweiuhr32we","status":"stopped","starttime":1234567890123,"operatingduration":421900}]
		// repositorystatus - JSON string providing the status of all the
		// repositories (example below)
		// [{"url":"hub.docker.com","linkstatus":"connected"},{"url":"188.65.2.81/containers","failed
		// login"}]
		// elementmessagecounts - JSON string providing the number of messages
		// published per element (example below)
		// [{"id":"d9823y23rewfouhSDFkh","messagecount":428},{"id":"978yerwfiouhASDFkjh","messagecount":8321}]

		postParams.put("daemonstatus", StatusReporter.getSupervisorStatus().getDaemonStatus());
		postParams.put("daemonoperatingduration", StatusReporter.getSupervisorStatus().getOperationDuration());
		postParams.put("daemonlaststart", StatusReporter.getSupervisorStatus().getDaemonLastStart());
		postParams.put("memoryusage", StatusReporter.getResourceConsumptionManagerStatus().getMemoryUsage());
		postParams.put("diskusage", StatusReporter.getResourceConsumptionManagerStatus().getDiskUsage());
		postParams.put("cpuusage", StatusReporter.getResourceConsumptionManagerStatus().getCpuUsage());
		postParams.put("memoryviolation", StatusReporter.getResourceConsumptionManagerStatus().isMemoryViolation());
		postParams.put("diskviolation", StatusReporter.getResourceConsumptionManagerStatus().isMemoryViolation());
		postParams.put("cpuviolation", StatusReporter.getResourceConsumptionManagerStatus().isMemoryViolation());
		postParams.put("elementstatus", StatusReporter.getResourceConsumptionManagerStatus().isMemoryViolation()); // TODO : elements status
		postParams.put("repositorycount", StatusReporter.getProcessManagerStatus().getRegistriesCount());
		postParams.put("repositorystatus", StatusReporter.getResourceConsumptionManagerStatus().isMemoryViolation()); // TODO : repositories status
		postParams.put("systemtime", StatusReporter.getStatusReporterStatus().getSystemTime());
		postParams.put("laststatustime", StatusReporter.getStatusReporterStatus().getLastUpdate());
		// TODO : other modules
		postParams.put("ipaddress", StatusReporter.getResourceConsumptionManagerStatus().isMemoryViolation());
		postParams.put("processedmessages", StatusReporter.getResourceConsumptionManagerStatus().isMemoryViolation());
		postParams.put("elementmessagecounts", StatusReporter.getResourceConsumptionManagerStatus().isMemoryViolation());
		postParams.put("messagespeed", StatusReporter.getResourceConsumptionManagerStatus().isMemoryViolation());
		postParams.put("lastcommandtime", StatusReporter.getResourceConsumptionManagerStatus().isMemoryViolation());

		try {
			JsonObject result = orchestrator.doCommand("status", null, postParams);
			if (!result.get("status").equals("ok"))
				throw new Exception("error from fabric controller");
		} catch (Exception e) {
			// TODO : sendStatus() failed. do something...
			LoggingService.logWarning(MODULE_NAME, "unable to send status : " + e.getMessage());
		}
	}
	
	private void getChangesList() {
		ElementManager elementManager = new ElementManager();
		// TODO : TEMPORARY *****************************************************
		LoggingService.logInfo(MODULE_NAME, "loading elements data");
		elementManager.loadFromApi();
		LoggingService.logInfo(MODULE_NAME, "elements data loaded");
		return;
		//***********************************************************************
		
//		Map<String, Object> queryParams = new HashMap<>();
//		queryParams.put("timestamp", lastGetChangesList);
//		
//		JSONObject result = null;
//		try {
//			result = orchestrator.doCommand("changes", queryParams, null);
//			if (!result.get("status").equals("ok"))
//				throw new Exception("error from fabric controller");
//		} catch (Exception e) {
//			// TODO : getChangesList() failed. do something...
//			LoggingService.logWarning(MODULE_NAME, "unable to get changes : " + e.getMessage());
//			return;
//		}
//
//		lastGetChangesList = System.currentTimeMillis();
//
//		JSONObject changes = (JSONObject) result.get("changes");
//		if (changes.get("config").equals("true")) {
//			// TODO: config changed
//		}
//		if (changes.get("containerlist").equals("true")) {
//			elementManager.loadElementsList();
//		}
//		if (changes.get("containerconfig").equals("true")) {
//			elementManager.loadElementsConfig();
//		}
//		if (changes.get("routing").equals("true")) {
//			elementManager.loadRoutes();
//		}
//		if (changes.get("registries").equals("true")) {
//			elementManager.loadRegistries();
//		}
	}
	
	public String doProvisioning(String key) {
		JsonObject result = null;
		try {
			result = orchestrator.provision(key);
			if (!result.get("status").equals("ok")) 
				throw new Exception("error from fabric controller");

			Configuration.setInstanceId(result.get("id").toString());
			Configuration.setAccessToken(result.get("token").toString());
		} catch (Exception e) {
			LoggingService.logWarning(MODULE_NAME, "provisioning failed - " + e.getMessage());
			return "\nFailure - " + e.getMessage();
		}
		return "\n\nSuccess - instance ID is " + Configuration.getInstanceId();
	}
	
	public static void main(String[] args) {
	}
}
