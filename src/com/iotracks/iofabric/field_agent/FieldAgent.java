package com.iotracks.iofabric.field_agent;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import com.iotracks.iofabric.element.Element;
import com.iotracks.iofabric.element.ElementManager;
import com.iotracks.iofabric.element.PortMapping;
import com.iotracks.iofabric.element.Registry;
import com.iotracks.iofabric.element.Route;
import com.iotracks.iofabric.field_agent.controller.APIServer;
import com.iotracks.iofabric.status_reporter.StatusReporter;
import com.iotracks.iofabric.utils.Constants.ContollerStatus;
import com.iotracks.iofabric.utils.Orchestrator;
import com.iotracks.iofabric.utils.configuration.Configuration;
import com.iotracks.iofabric.utils.logging.LoggingService;

public class FieldAgent {
	private final String MODULE_NAME = "Field Agent";
	private final int GET_CHANGES_LIST_FREQ_SECONDS = 30;
	private final int CHECK_CONTROLLER_FREQ_SECONDS = 60;
	private final String filesPath = "/etc/iofabric/";

	private Orchestrator orchestrator;
	private long lastGetChangesList;
	private ElementManager elementManager;
	private static FieldAgent instance;

	private FieldAgent() {
		lastGetChangesList = 0;
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

	private final Runnable getChangesList = () -> {
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

		JsonObject changes = (JsonObject) result.get("changes");
		if (changes.getBoolean("config")) {
			// TODO: config changed
		}
		if (changes.getBoolean("containerlist")) {
			loadElementsList(false);
		}
		if (changes.getBoolean("containerconfig")) {
			loadElementsConfig(false);
		}
		if (changes.getBoolean("routing")) {
			loadRoutes(false);
		}
		if (changes.getBoolean("registries")) {
			loadRegistries(false);
		}
	};

	public void loadRegistries(boolean fromFile) {
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
				saveFile(registriesList.toString(), filesPath + filename);
			}

			List<Registry> registries = new ArrayList<>(); 
			for (int i = 0; i < registriesList.size(); i++) {
				JsonObject registry = registriesList.getJsonObject(i);
				Registry r = new Registry();
				r.setUrl(registry.getString("url"));
				r.setSecure(registry.getBoolean("secure"));
				r.setCertificate(registry.getString("certificate"));
				r.setRequiersCertificate(registry.getBoolean("requirescert"));
				r.setUserName(registry.getString("username"));
				r.setPassword(registry.getString("password"));
				r.setUserEmail(registry.getString("useremail"));
				registries.add(r);
			}
			elementManager.setRegistries(registries);
		} catch (Exception e) {
			LoggingService.logWarning(MODULE_NAME, "unable to get registries : " + e.getMessage());
		}
	}

	private void loadElementsConfig(boolean fromFile) {
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
				saveFile(configs.toString(), filesPath + filename);
			}

			Map<String, String> cfg = new HashMap<>();
			for (int i = 0; i < configs.size(); i++) {
				JsonObject config = configs.getJsonObject(i);
				String id = config.getString("id");
				String configString = config.getString("config");
				long lastUpdated = config.getJsonNumber("lastupdatedtimestamp").longValue();
				cfg.put(id, configString);
			}
			elementManager.setConfigs(cfg);
		} catch (Exception e) {
			LoggingService.logWarning(MODULE_NAME, "unable to get elements config : " + e.getMessage());
		}
	}

	private void loadRoutes(boolean fromFile) {
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
				saveFile(routes.toString(), filesPath + filename);
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
				saveFile(containers.toString(), filesPath + filename);
			}

			List<Element> elements = new ArrayList<>();
			for (int i = 0; i < containers.size(); i++) {
				JsonObject container = containers.getJsonObject(i);

				Element element = new Element(container.getString("id"), container.getString("imageid"));

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

	public void start() {
		elementManager = ElementManager.getInstance();
		orchestrator = new Orchestrator();
		
		loadElementsList(true);
		loadElementsConfig(true);
		loadRoutes(true);
		loadRegistries(true);
		
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(getChangesList, 0, GET_CHANGES_LIST_FREQ_SECONDS, TimeUnit.SECONDS);
		scheduler.scheduleAtFixedRate(pingController, 0, CHECK_CONTROLLER_FREQ_SECONDS, TimeUnit.SECONDS);
	}
	
	private final Runnable pingController = () -> {
		if (orchestrator.ping()) {
			StatusReporter.setFieldAgentStatus().setContollerStatus(ContollerStatus.OK);
			StatusReporter.setFieldAgentStatus().setControllerVerified(true);
		} else {
			StatusReporter.setFieldAgentStatus().setContollerStatus(ContollerStatus.BROKEN);
			StatusReporter.setFieldAgentStatus().setControllerVerified(false);
		}
		StatusReporter.setFieldAgentStatus().setLastCommandTime(lastGetChangesList);
	};

	private void sendStatus() {
		Map<String, Object> postParams = new HashMap<>();

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
	
	private String checksum(String data) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA1");
			md.update(data.getBytes());
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
			byte[] encoded = Files.readAllBytes(Paths.get(filename));
			String lines = new String(encoded, StandardCharsets.US_ASCII);
			String data = lines.substring(lines.indexOf('\n') + 1);
			String checksum = lines.substring(1, lines.indexOf('"', 1));
			if (!checksum(data).equals(checksum)) {
				return null;
			}
			JsonReader reader = Json.createReader(new StringReader(data));
			JsonArray result = reader.readArray();			
			return result;
		} catch (Exception e) {
			return null;
		}
	}
	
	private void getFabricConfig() {
		
	}
	
	private void saveFile(String data, String filename) {
		try {
			String checksum = checksum(data);
			Files.write(Paths.get(filename), String.format("\"%s\"\n%s", checksum, data).getBytes());
		} catch (Exception e) {}
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
	
	public static void main(String[] args) throws Exception {
		Configuration.loadConfig();
		LoggingService.setupLogger();
		
		FieldAgent fieldAgent = new FieldAgent();
		APIServer server = new APIServer();
		server.start();
		fieldAgent.start();
		
		while (true);
	}
}
