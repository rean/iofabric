package com.iotracks.iofabric.field_agent;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;

import com.iotracks.iofabric.element.Element;
import com.iotracks.iofabric.element.ElementManager;
import com.iotracks.iofabric.element.PortMapping;
import com.iotracks.iofabric.element.Registry;
import com.iotracks.iofabric.element.Route;
import com.iotracks.iofabric.field_agent.controller.APIServer;
import com.iotracks.iofabric.local_api.LocalApi;
import com.iotracks.iofabric.message_bus.MessageBus;
import com.iotracks.iofabric.process_manager.ProcessManager;
import com.iotracks.iofabric.status_reporter.StatusReporter;
import com.iotracks.iofabric.supervisor.Supervisor;
import com.iotracks.iofabric.utils.Constants.ControllerStatus;
import com.iotracks.iofabric.utils.Orchestrator;
import com.iotracks.iofabric.utils.configuration.Configuration;
import com.iotracks.iofabric.utils.logging.LoggingService;

public class FieldAgent {
	private final String MODULE_NAME = "Field Agent";
	private final int GET_CHANGES_LIST_FREQ_SECONDS = 30;
	private final int CHECK_CONTROLLER_FREQ_SECONDS = 60;
	private final int POST_STATUS_FREQ_SECONDS = 30;
	private final String filesPath = "/etc/iofabric/";

	private Orchestrator orchestrator;
	private long lastGetChangesList;
	private ElementManager elementManager;
	private static FieldAgent instance;
	private boolean firstTime;

	private FieldAgent() {
		lastGetChangesList = 0;
		firstTime = true;
	}
	
	public static FieldAgent getInstance() {
		if (instance == null) {
			synchronized (FieldAgent.class) {
				if (instance == null) 
					instance = new FieldAgent();
			}
		}
		return instance;
	}
	
	private final Runnable postStatus = () -> {
		LoggingService.logInfo(MODULE_NAME, "post status");
		if (StatusReporter.getFieldAgentStatus().getContollerStatus().equals(ControllerStatus.NOT_PROVISIONED)) {
			LoggingService.logWarning(MODULE_NAME, "not provisioned");
			return;
		}
		
		if (StatusReporter.getFieldAgentStatus().getContollerStatus().equals(ControllerStatus.BROKEN)) {
			LoggingService.logWarning(MODULE_NAME, "connection to controller has broken");
			return;
		}
		
		Map<String, Object> postParams = new HashMap<>();

		postParams.put("daemonstatus", StatusReporter.getSupervisorStatus().getDaemonStatus());
		postParams.put("daemonoperatingduration", StatusReporter.getSupervisorStatus().getOperationDuration());
		postParams.put("daemonlaststart", StatusReporter.getSupervisorStatus().getDaemonLastStart());
		postParams.put("memoryusage", StatusReporter.getResourceConsumptionManagerStatus().getMemoryUsage());
		postParams.put("diskusage", StatusReporter.getResourceConsumptionManagerStatus().getDiskUsage());
		postParams.put("cpuusage", StatusReporter.getResourceConsumptionManagerStatus().getCpuUsage());
		postParams.put("memoryviolation", StatusReporter.getResourceConsumptionManagerStatus().isMemoryViolation() ? "yes" : "no");
		postParams.put("diskviolation", StatusReporter.getResourceConsumptionManagerStatus().isDiskViolation() ? "yes" : "no");
		postParams.put("cpuviolation", StatusReporter.getResourceConsumptionManagerStatus().isCpuViolation() ? "yes" : "no");
		postParams.put("elementstatus", StatusReporter.getProcessManagerStatus().getJsonElementsStatus());
		postParams.put("repositorycount", StatusReporter.getProcessManagerStatus().getRegistriesCount());
		postParams.put("repositorystatus", StatusReporter.getProcessManagerStatus().getJsonRegistriesStatus());
		postParams.put("systemtime", StatusReporter.getStatusReporterStatus().getSystemTime());
		postParams.put("laststatustime", StatusReporter.getStatusReporterStatus().getLastUpdate());
		postParams.put("ipaddress", StatusReporter.getResourceConsumptionManagerStatus().isMemoryViolation());
		postParams.put("processedmessages", StatusReporter.getMessageBusStatus().getProcessedMessages());
		postParams.put("elementmessagecounts", StatusReporter.getMessageBusStatus().getJsonPublishedMessagesPerElement());
		postParams.put("messagespeed", StatusReporter.getMessageBusStatus().getAverageSpeed());
		postParams.put("lastcommandtime", StatusReporter.getFieldAgentStatus().getLastCommandTime());

		try {
			JsonObject result = orchestrator.doCommand("status", null, postParams);
			if (!result.getString("status").equals("ok"))
				throw new Exception("error from fabric controller");
		} catch (Exception e) {
			LoggingService.logWarning(MODULE_NAME, "unable to send status : " + e.getMessage());
		}
	};
	
	private final Runnable getChangesList = () -> {
		LoggingService.logInfo(MODULE_NAME, "get changes list");
		if (StatusReporter.getFieldAgentStatus().getContollerStatus().equals(ControllerStatus.NOT_PROVISIONED)) {
			LoggingService.logWarning(MODULE_NAME, "not provisioned");
			return;
		}
		
		if (StatusReporter.getFieldAgentStatus().getContollerStatus().equals(ControllerStatus.BROKEN)) {
			LoggingService.logWarning(MODULE_NAME, "connection to controller has broken");
			return;
		}
		
		Map<String, Object> queryParams = new HashMap<>();
		queryParams.put("timestamp", lastGetChangesList);
		
		JsonObject result = null;
		try {
			result = orchestrator.doCommand("changes", queryParams, null);
			if (!result.getString("status").equals("ok"))
				throw new Exception("error from fabric controller");
		} catch (Exception e) {
			LoggingService.logWarning(MODULE_NAME, "unable to get changes : " + e.getMessage());
			return;
		}

		lastGetChangesList = result.getJsonNumber("timestamp").longValue();

		JsonObject changes = result.getJsonObject("changes");
		boolean changed = false;
		if (changes.getBoolean("config") && !firstTime)
			getFabricConfig();
		
		if (changes.getBoolean("containerlist") || firstTime) {
			loadElementsList(false);
			changed = true;
		}
		if (changes.getBoolean("containerconfig") || firstTime) {
			loadElementsConfig(false);
			changed = true;
		}
		if (changes.getBoolean("routing") || firstTime) {
			loadRoutes(false);
			changed = true;
		}
		if (changes.getBoolean("registries") || firstTime) {
			loadRegistries(false);
			changed = true;
		}
		if (changed)
			notifyModules();
		
		firstTime = false;
	};
	
	public void loadRegistries(boolean fromFile) {
		LoggingService.logInfo(MODULE_NAME, "get registries");
		if (StatusReporter.getFieldAgentStatus().getContollerStatus().equals(ControllerStatus.NOT_PROVISIONED)) {
			LoggingService.logWarning(MODULE_NAME, "not provisioned");
			return;
		}
		
		if (StatusReporter.getFieldAgentStatus().getContollerStatus().equals(ControllerStatus.BROKEN) && !fromFile) {
			LoggingService.logWarning(MODULE_NAME, "connection to controller has broken");
			return;
		}
		
		String filename = "registries.json";
		try {
			JsonArray registriesList = null;
			if (fromFile) {
				registriesList = readFile(filesPath + filename);
				if (registriesList == null)
					throw new Exception("cannot read from file");
			} else {
				JsonObject result = orchestrator.doCommand("registries", null, null);
				if (!result.getString("status").equals("ok"))				
					throw new Exception("error from fabric controller");

				registriesList = result.getJsonArray("registries");
				saveFile(registriesList, filesPath + filename);
			}

			List<Registry> registries = new ArrayList<>();
			for (int i = 0; i < registriesList.size(); i++) {
				JsonObject registry = registriesList.getJsonObject(i);
				Registry result = new Registry();
				result.setUrl(registry.getString("url"));
				result.setSecure(registry.getBoolean("secure"));
				result.setCertificate(registry.getString("certificate"));
				result.setRequiersCertificate(registry.getBoolean("requirescert"));
				result.setUserName(registry.getString("username"));
				result.setPassword(registry.getString("password"));
				result.setUserEmail(registry.getString("useremail"));
				registries.add(result);
			}
			elementManager.setRegistries(registries);
		} catch (Exception e) {
			LoggingService.logWarning(MODULE_NAME, "unable to get registries : " + e.getMessage());
		}
	}

	private void notifyModules() {
		MessageBus.getInstance().update();
		ProcessManager.getInstance().update();
		LocalApi.getInstance().update();
		
	}

	private void loadElementsConfig(boolean fromFile) {
		LoggingService.logInfo(MODULE_NAME, "get elemets config");
		if (StatusReporter.getFieldAgentStatus().getContollerStatus().equals(ControllerStatus.NOT_PROVISIONED)) {
			LoggingService.logWarning(MODULE_NAME, "not provisioned");
			return;
		}
		
		if (StatusReporter.getFieldAgentStatus().getContollerStatus().equals(ControllerStatus.BROKEN) && !fromFile) {
			LoggingService.logWarning(MODULE_NAME, "connection to controller has broken");
			return;
		}
		
		String filename = "configs.json";
		try {
			JsonArray configs = null;
			if (fromFile) {
				configs = readFile(filesPath + filename);
				if (configs == null)
					throw new Exception("cannot read from file");
			} else {
				JsonObject result = orchestrator.doCommand("containerconfig", null, null);
				if (!result.getString("status").equals("ok"))
					throw new Exception("error from fabric controller");
				configs = result.getJsonArray("containerconfig");
				saveFile(configs, filesPath + filename);
			}

			Map<String, String> cfg = new HashMap<>();
			for (int i = 0; i < configs.size(); i++) {
				JsonObject config = configs.getJsonObject(i);
				String id = config.getString("id");
				String configString = config.getString("config");
//				long lastUpdated = config.getJsonNumber("lastupdatedtimestamp").longValue();
				cfg.put(id, configString);
			}
			elementManager.setConfigs(cfg);
		} catch (Exception e) {
			LoggingService.logWarning(MODULE_NAME, "unable to get elements config : " + e.getMessage());
		}
	}

	private void loadRoutes(boolean fromFile) {
		LoggingService.logInfo(MODULE_NAME, "get routes");
		if (StatusReporter.getFieldAgentStatus().getContollerStatus().equals(ControllerStatus.NOT_PROVISIONED)) {
			LoggingService.logWarning(MODULE_NAME, "not provisioned");
			return;
		}
		
		if (StatusReporter.getFieldAgentStatus().getContollerStatus().equals(ControllerStatus.BROKEN) && !fromFile) {
			LoggingService.logWarning(MODULE_NAME, "connection to controller has broken");
			return;
		}
		
		String filename = "routes.json";
		try {
			JsonArray routes = null;
			if (fromFile) {
				routes = readFile(filesPath + filename);
				if (routes == null)
					throw new Exception("cannot read from file");
			} else {
				JsonObject result = orchestrator.doCommand("routing", null, null);
				if (!result.getString("status").equals("ok"))
					throw new Exception("error from fabric controller");
				routes = result.getJsonArray("routing");
				saveFile(routes, filesPath + filename);
			}

			Map<String, Route> r = new HashMap<>();
			for (int i = 0; i < routes.size(); i++) {
				JsonObject route = routes.getJsonObject(i);
				Route elementRoute = new Route();
				String container = route.getString("container");

				JsonArray receivers = route.getJsonArray("receivers");
				if (receivers.size() == 0)
					continue;
				for (int j = 0; j < receivers.size(); j++) {
					String receiver = receivers.getString(j);
					elementRoute.getReceivers().add(receiver);
				}
				r.put(container, elementRoute);
			}
			elementManager.setRoutes(r);
		} catch (Exception e) {
			LoggingService.logWarning(MODULE_NAME, "unable to get routing" + e.getMessage());
		}
	}

	private void loadElementsList(boolean fromFile) {
		LoggingService.logInfo(MODULE_NAME, "get elements");
		if (StatusReporter.getFieldAgentStatus().getContollerStatus().equals(ControllerStatus.NOT_PROVISIONED)) {
			LoggingService.logWarning(MODULE_NAME, "not provisioned");
			return;
		}
		
		if (StatusReporter.getFieldAgentStatus().getContollerStatus().equals(ControllerStatus.BROKEN) && !fromFile) {
			LoggingService.logWarning(MODULE_NAME, "connection to controller has broken");
			return;
		}
		
		String filename = "elements.json";
		try {
			JsonArray containers = null;
			if (fromFile) {
				containers = readFile(filesPath + filename);
				if (containers == null)
					throw new Exception("cannot read from file");
			} else {
				JsonObject result = orchestrator.doCommand("containerlist", null, null);
				if (!result.getString("status").equals("ok"))
					throw new Exception("error from fabric controller");
				containers = result.getJsonArray("containerlist");
				saveFile(containers, filesPath + filename);
			}

			List<Element> elements = new ArrayList<>();
			for (int i = 0; i < containers.size(); i++) {
				JsonObject container = containers.getJsonObject(i);

				Element element = new Element(container.getString("id"), container.getString("imageid"));
				element.setRebuild(container.getBoolean("rebuild"));
				element.setRegistry(container.getString("registryurl"));
				element.setLastModified(container.getJsonNumber("lastmodified").longValue());

				JsonArray portMappingObjs = container.getJsonArray("portmappings");
				List<PortMapping> pms = null;
				if (portMappingObjs.size() > 0) {
					pms = new ArrayList<>();
					for (int j = 0; j < portMappingObjs.size(); j++) {
						JsonObject portMapping = portMappingObjs.getJsonObject(j);
						PortMapping pm = new PortMapping(portMapping.getString("outsidecontainer"),
								portMapping.getString("insidecontainer"));
						pms.add(pm);
					}
				}
				element.setPortMappings(pms);
				elements.add(element);
			}
			elementManager.setElements(elements);
		} catch (Exception e) {
			LoggingService.logWarning(MODULE_NAME, "unable to get containers list" + e.getMessage());
		}
	}

	private final Runnable pingController = () -> {
		LoggingService.logInfo(MODULE_NAME, "ping controller");
		if (StatusReporter.getFieldAgentStatus().getContollerStatus().equals(ControllerStatus.NOT_PROVISIONED)) {
			LoggingService.logWarning(MODULE_NAME, "not provisioned");
			return;
		}
		
		if (orchestrator.ping()) {
			StatusReporter.setFieldAgentStatus().setContollerStatus(ControllerStatus.OK);
			StatusReporter.setFieldAgentStatus().setControllerVerified(true);
		} else {
			StatusReporter.setFieldAgentStatus().setContollerStatus(ControllerStatus.BROKEN);
			StatusReporter.setFieldAgentStatus().setControllerVerified(false);
		}
		StatusReporter.setFieldAgentStatus().setLastCommandTime(lastGetChangesList);
	};

	private String checksum(String data) {
		try {
			byte[] base64 = Base64.getEncoder().encode(data.getBytes(StandardCharsets.US_ASCII));
			MessageDigest md = MessageDigest.getInstance("SHA1");
			md.update(base64);
			byte[] mdbytes = md.digest();
			StringBuffer sb = new StringBuffer("");
			for (int i = 0; i < mdbytes.length; i++) {
				sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
			}
			return sb.toString();
		} catch (Exception e) {
			return "";
		}
	}

	private JsonArray readFile(String filename) {
		try {
			if (!Files.exists(Paths.get(filename), LinkOption.NOFOLLOW_LINKS))
				return null;
			
			JsonReader reader = Json.createReader(new FileReader(new File(filename)));
			JsonObject object = reader.readObject();
			reader.close();
			String checksum = object.getString("checksum");
			JsonArray data = object.getJsonArray("data");
			if (!checksum(data.toString()).equals(checksum))
				return null;
			return data;
		} catch (Exception e) {
			return null;
		}
	}
	
	private void saveFile(JsonArray data, String filename) {
		try {
			String checksum = checksum(data.toString());
			JsonObject object = Json.createObjectBuilder()
					.add("checksum", checksum)
					.add("data", data)
					.build();
			JsonWriter writer = Json.createWriter(new FileWriter(new File(filename)));
			writer.writeObject(object);
			writer.close();
		} catch (Exception e) {}
	}
	
	private void getFabricConfig() {
		LoggingService.logInfo(MODULE_NAME, "get fabric config");
		if (StatusReporter.getFieldAgentStatus().getContollerStatus().equals(ControllerStatus.NOT_PROVISIONED)) {
			LoggingService.logWarning(MODULE_NAME, "not provisioned");
			return;
		}
		
		if (StatusReporter.getFieldAgentStatus().getContollerStatus().equals(ControllerStatus.BROKEN)) {
			LoggingService.logWarning(MODULE_NAME, "connection to controller has broken");
			return;
		}
		
		if (firstTime) {
			postFabricConfig();
			return;
		}
		try {
			JsonObject result = orchestrator.doCommand("config", null, null);
			if (!result.getString("status").equals("ok"))
				throw new Exception("error from fabric controller");
			
			JsonObject configs = result.getJsonObject("config");
			String networkInterface = configs.getString("networkinterface");
			String dockerUrl = configs.getString("dockerurl");
			float diskLimit = (float) configs.getJsonNumber("disklimit").doubleValue();
			String diskDirectory = configs.getString("diskdirectory");
			float memoryLimit = (float) configs.getJsonNumber("memorylimit").doubleValue();
			float cpuLimit = (float) configs.getJsonNumber("cpulimit").doubleValue();
			float logLimit = (float) configs.getJsonNumber("loglimit").doubleValue();
			String logDirectory = configs.getString("logdirectory");
			int logFileCount = configs.getInt("logfilecount");
			
			Map<String, Object> instanceConfig = new HashMap<>();
			
			if (!Configuration.getNetworkInterface().equals(networkInterface))
				instanceConfig.put("n", networkInterface);
			
			if (!Configuration.getDockerUrl().equals(dockerUrl))
				instanceConfig.put("c", dockerUrl);

			if (Configuration.getDiskLimit() != diskLimit)
				instanceConfig.put("d", diskLimit);
			
			if (!Configuration.getDiskDirectory().equals(diskDirectory))
				instanceConfig.put("dl", diskDirectory);

			if (Configuration.getMemoryLimit() != memoryLimit)
				instanceConfig.put("m", memoryLimit);
			
			if (Configuration.getCpuLimit() != cpuLimit)
				instanceConfig.put("p", cpuLimit);
			
			if (Configuration.getLogDiskLimit() != logLimit)
				instanceConfig.put("l", logLimit);
			
			if (!Configuration.getLogDiskDirectory().equals(logDirectory))
				instanceConfig.put("ld", logDirectory);
			
			if (Configuration.getLogFileCount() != logFileCount)
				instanceConfig.put("lc", logFileCount);
			
			if (!instanceConfig.isEmpty())
				Configuration.setConfig(instanceConfig);
			
		} catch (Exception e) {
			LoggingService.logWarning(MODULE_NAME, "unable to get fabric config : " + e.getMessage());
		}
	}
	
	public void postFabricConfig() {
		LoggingService.logInfo(MODULE_NAME, "post fabric config");
		if (StatusReporter.getFieldAgentStatus().getContollerStatus().equals(ControllerStatus.NOT_PROVISIONED)) {
			LoggingService.logWarning(MODULE_NAME, "not provisioned");
			return;
		}
		
		if (StatusReporter.getFieldAgentStatus().getContollerStatus().equals(ControllerStatus.BROKEN)) {
			LoggingService.logWarning(MODULE_NAME, "connection to controller has broken");
			return;
		}
		
		try {
			Map<String, Object> postParams = new HashMap<>();
			postParams.put("networkinterface", Configuration.getNetworkInterface());
			postParams.put("dockerurl", Configuration.getDockerUrl());
			postParams.put("disklimit", Configuration.getDiskLimit());
			postParams.put("diskdirectory", Configuration.getDiskDirectory());
			postParams.put("memorylimit", Configuration.getMemoryLimit());
			postParams.put("cpulimit", Configuration.getCpuLimit());
			postParams.put("loglimit", Configuration.getLogDiskLimit());
			postParams.put("logdirectory", Configuration.getLogDiskDirectory());
			postParams.put("logfilecount", Configuration.getLogFileCount());
			
			JsonObject result = orchestrator.doCommand("config/changes", null, postParams);
			if (!result.getString("status").equals("ok"))
				throw new Exception("error from fabric controller");
		} catch (Exception e) {
			LoggingService.logWarning(MODULE_NAME, "unable to post fabric config : " + e.getMessage());
		}
	}
	
	public String provision(String key) {
		LoggingService.logInfo(MODULE_NAME, "provisioning");
		try {
			JsonObject result = orchestrator.provision(key);
			if (!result.getString("status").equals("ok")) 
				throw new Exception("error from fabric controller");

			StatusReporter.setFieldAgentStatus().setContollerStatus(ControllerStatus.OK);
			Configuration.setInstanceId(result.getString("id"));
			Configuration.setAccessToken(result.getString("token"));
			try {
				Configuration.saveConfigUpdates();
			} catch (Exception e) {}

			loadElementsList(false);
			loadElementsConfig(false);
			loadRoutes(false);
			loadRegistries(false);
			notifyModules();
			
			return String.format("\nSuccess - instance ID is %s", result.getString("id"));
		} catch (Exception e) {
			LoggingService.logWarning(MODULE_NAME, "provisioning failed - " + e.getMessage());
			return "\nProvisioning failed";
		}
	}
	
	public String deProvision() {
		LoggingService.logInfo(MODULE_NAME, "deprovisioning");
		if (StatusReporter.getFieldAgentStatus().getContollerStatus().equals(ControllerStatus.NOT_PROVISIONED)) {
			LoggingService.logWarning(MODULE_NAME, "not provisioned");
			return "\nFailure - not provisioned";
		}
		
		if (StatusReporter.getFieldAgentStatus().getContollerStatus().equals(ControllerStatus.BROKEN)) {
			LoggingService.logWarning(MODULE_NAME, "connection to controller has broken");
			return "\nFailure - not connected to controller";
		}
		
		StatusReporter.setFieldAgentStatus().setContollerStatus(ControllerStatus.NOT_PROVISIONED);
		Configuration.setInstanceId("");
		Configuration.setAccessToken("");
		try {
			Configuration.saveConfigUpdates();
		} catch (Exception e) {}
		elementManager.clearData();
		notifyModules();
		return "\nSuccess - tokens and identifiers and keys removed";
	}
	
	public void instanceConfigUpdated() {
		orchestrator.update();
	}
	
	public void start() {
		//TODO:  TEMPORARY ***************************
		APIServer server = new APIServer();
		server.start();
		// *******************************************
		
		if (Configuration.getInstanceId() == null || Configuration.getInstanceId().equals("")
				|| Configuration.getAccessToken() == null || Configuration.getAccessToken().equals(""))
			StatusReporter.setFieldAgentStatus().setContollerStatus(ControllerStatus.NOT_PROVISIONED);
			
		elementManager = ElementManager.getInstance();
		orchestrator = new Orchestrator();
		
		Supervisor.scheduler.scheduleAtFixedRate(pingController, 0, CHECK_CONTROLLER_FREQ_SECONDS, TimeUnit.SECONDS);
		try {
			Thread.sleep(200);
		} catch (Exception e) {}
		getFabricConfig();
		if (!StatusReporter.getFieldAgentStatus().getContollerStatus().equals(ControllerStatus.NOT_PROVISIONED)) {
			loadElementsList(true);
			loadElementsConfig(true);
			loadRoutes(true);
			loadRegistries(true);
		}
		Supervisor.scheduler.scheduleAtFixedRate(getChangesList, 0, GET_CHANGES_LIST_FREQ_SECONDS, TimeUnit.SECONDS);
		Supervisor.scheduler.scheduleAtFixedRate(postStatus, POST_STATUS_FREQ_SECONDS, POST_STATUS_FREQ_SECONDS, TimeUnit.SECONDS);
	}
}
