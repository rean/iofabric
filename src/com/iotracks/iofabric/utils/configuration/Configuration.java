package com.iotracks.iofabric.utils.configuration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.iotracks.iofabric.field_agent.FieldAgent;
import com.iotracks.iofabric.message_bus.MessageBus;
import com.iotracks.iofabric.process_manager.ProcessManager;
import com.iotracks.iofabric.resource_consumption_manager.ResourceConsumptionManager;
import com.iotracks.iofabric.utils.Orchestrator;
import com.iotracks.iofabric.utils.logging.LoggingService;

/**
 * holds IOFabric instance configuration
 * 
 * @author saeid
 *
 */
public final class Configuration {

	private static Element configElement;
	private static Document configFile;

	private static String accessToken;
	private static String instanceId;
	private static String controllerUrl;
	private static String controllerCert;
	private static String networkInterface;
	private static String dockerUrl;
	private static float diskLimit;
	private static float memoryLimit;
	private static String diskDirectory;
	private static float cpuLimit;
	private static float logDiskLimit;
	private static String logDiskDirectory;
	private static int logFileCount;

	public static boolean debugging = false;

	/**
	 * return XML node value
	 * 
	 * @param name - node name
	 * @return node value
	 * @throws ConfigurationItemException
	 */
	private static String getNode(String name) throws ConfigurationItemException {
		NodeList nodes = configElement.getElementsByTagName(name);
		if (nodes.getLength() != 1)
			throw new ConfigurationItemException("<" + name + "> item not found or defined more than once");

		return nodes.item(0).getTextContent();
	}

	/**
	 * sets XML node value
	 * 
	 * @param name - node name
	 * @param content - node value
	 * @throws ConfigurationItemException
	 */
	private static void setNode(String name, String content) throws ConfigurationItemException {

		NodeList nodes = configFile.getElementsByTagName(name);

		if (nodes.getLength() != 1)
			throw new ConfigurationItemException("<" + name + "> item not found or defined more than once");

		nodes.item(0).setTextContent(content);

	}

	public static HashMap<String, String> getOldNodeValuesForParameters(Set<String> parameters) throws ConfigurationItemException{

		HashMap<String, String> result = new HashMap<String, String>();

		for(String option : parameters){
			switch (option) {
			case "d":
				result.put(option, getNode("disk_consumption_limit"));
				break;
			case "dl":
				result.put(option, getNode("disk_directory"));
				break;
			case "m":
				result.put(option, getNode("memory_consumption_limit"));
				break;
			case "p":
				result.put(option, getNode("processor_consumption_limit"));
				break;
			case "a":
				result.put(option, getNode("controller_url"));
				break;
			case "ac":
				result.put(option, getNode("controller_cert"));
				break;
			case "c":
				result.put(option, getNode("docker_url"));
				break;
			case "n":
				result.put(option, getNode("network_interface"));
				break;
			case "l":
				result.put(option, getNode("log_disk_consumption_limit"));
				break;
			case "ld":
				result.put(option, getNode("log_disk_directory"));
				break;
			case "lc":
				result.put(option, getNode("log_file_count"));
				break;
			default:
				throw new ConfigurationItemException("Invalid parameter -" + option);
			}

		}

		return result;
	}

	/**
	 * saves configuration data to config.xml
	 * and informs other modules
	 * 
	 * @throws Exception
	 */
	public static void saveConfigUpdates() throws Exception {
		FieldAgent.getInstance().instanceConfigUpdated();
		ProcessManager.getInstance().instanceConfigUpdated();
		ResourceConsumptionManager.getInstance().instanceConfigUpdated();
		LoggingService.instanceConfigUpdated();
		MessageBus.getInstance().instanceConfigUpdated();

		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		StreamResult result = new StreamResult(new File("/etc/iofabric/config.xml"));
		DOMSource source = new DOMSource(configFile);
		transformer.transform(source, result);
	}

	/**
	 * sets configuration base on commandline parameters
	 * 
	 * @param commandLineMap - map of config parameters
	 * @throws Exception
	 */
	public static void setConfig(Map<String, Object> commandLineMap) throws Exception {
		for (Map.Entry<String, Object> command : commandLineMap.entrySet()) {
			String option = command.getKey();
			String value = command.getValue().toString();

			if(option == null || value == null || value.trim() == "" || option.trim() == ""){
				throw new ConfigurationItemException("Command or value is invalid");
			}

			switch (option) {
			case "d":
				validateValue(option, value, "isPositiveFloat");
				setDiskLimit(Float.parseFloat(value));
				break;
			case "dl":
				value = addSeparator(value);
				setDiskDirectory(value);
				break;
			case "m":
				validateValue(option, value, "isPositiveFloat");
				setMemoryLimit(Float.parseFloat(value));
				break;
			case "p":
				validateValue(option, value, "isPositiveFloat");
				setCpuLimit(Float.parseFloat(value));
				break;
			case "a":
				setControllerUrl(value);
				break;
			case "ac":
				setControllerCert(value);
				break;
			case "c":
				setDockerUrl(value);
				break;
			case "n":
				setNetworkInterface(value);
				break;
			case "l":
				validateValue(option, value, "isPositiveFloat");
				setLogDiskLimit(Float.parseFloat(value));
				break;
			case "ld":
				value = addSeparator(value);
				setLogDiskDirectory(value);
				break;
			case "lc":
				validateValue(option, value, "isPositiveInteger");
				setLogFileCount(Integer.parseInt(value));
				break;
			default:
				throw new ConfigurationItemException("Invalid parameter -" + option);
			}

		}
		
		setChangedNodeValues(commandLineMap);
		saveConfigUpdates();
	}

	public static void setChangedNodeValues(Map<String, Object> commandLineMap) throws Exception {
		for (Map.Entry<String, Object> command : commandLineMap.entrySet()) {
			String option = command.getKey();
			String value = command.getValue().toString();

			switch (option) {
			case "d":
				setNode("disk_consumption_limit", value);
				break;
			case "dl":
				value = addSeparator(value);
				setNode("disk_directory", value);
				break;
			case "m":
				validateValue(option, value, "isPositiveFloat");
				setNode("memory_consumption_limit", value);
				break;
			case "p":
				validateValue(option, value, "isPositiveFloat");
				setNode("processor_consumption_limit", value);
				break;
			case "a":
				setNode("controller_url", value);
				break;
			case "ac":
				setNode("controller_cert", value);
				break;
			case "c":
				setNode("docker_url", value);
				break;
			case "n":
				setNode("network_interface", value);
				break;
			case "l":
				validateValue(option, value, "isPositiveFloat");
				setNode("log_disk_consumption_limit", value);
				break;
			case "ld":
				value = addSeparator(value);
				setNode("log_disk_directory", value);
				break;
			case "lc":
				validateValue(option, value, "isPositiveInteger");
				setNode("log_file_count", value);
				break;
			default:
				throw new ConfigurationItemException("Invalid parameter -" + option);
			}
		}

	}

	/**
	 * adds file separator to end of directory names, if not exists 
	 * 
	 * @param value - name of directory
	 * @return directory containing file separator at the end 
	 */
	private static String addSeparator(String value) {
		if (value.charAt(value.length() - 1) == File.separatorChar)
			return value;
		else
			return value + File.separatorChar;
	}

	/**
	 * validates value
	 * 
	 * @param option - config parameter
	 * @param value - value to be validated
	 * @param typeOfValidation - type of validation
	 * @throws ConfigurationItemException
	 */
	private static void validateValue(String option, String value, String typeOfValidation) throws ConfigurationItemException {
		if(typeOfValidation == "isPositiveFloat"){
			if (!value.matches("[0-9]*.?[0-9]*"))
				throw new ConfigurationItemException("Option -" + option + " has invalid value: " + value);
		}else if(typeOfValidation == "isPositiveInteger"){
			if (!value.matches("[0-9]*"))
				throw new ConfigurationItemException("Option -" + option + " has invalid value: " + value);
		}
	}

	/**
	 * loads configuration from config.xml file
	 * 
	 * @throws Exception
	 */
	public static void loadConfig() throws Exception {
		// TODO: load configuration XML file here
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		configFile = builder.parse("/etc/iofabric/config.xml");
		configFile.getDocumentElement().normalize();

		NodeList nodes = configFile.getElementsByTagName("config");
		if (nodes.getLength() != 1) {
			throw new ConfigurationItemException("<config> element not found or defined more than once");
		}
		configElement = (Element) nodes.item(0);

		setInstanceId(getNode("instance_id"));
		setAccessToken(getNode("access_token"));
		setControllerUrl(getNode("controller_url"));
		setControllerCert(getNode("controller_cert"));
		setNetworkInterface(getNode("network_interface"));
		setDockerUrl(getNode("docker_url"));
		setDiskLimit(Float.parseFloat(getNode("disk_consumption_limit")));
		setDiskDirectory(getNode("disk_directory"));
		setMemoryLimit(Float.parseFloat(getNode("memory_consumption_limit")));
		setCpuLimit(Float.parseFloat(getNode("processor_consumption_limit")));
		setLogDiskDirectory(getNode("log_disk_directory"));
		setLogDiskLimit(Float.parseFloat(getNode("log_disk_consumption_limit")));
		setLogFileCount(Integer.parseInt(configElement.getElementsByTagName("log_file_count").item(0).getTextContent()));
	}

	private Configuration() {

	}

	public static String getAccessToken() {
		return accessToken;
	}

	public static String getControllerUrl() {
		return controllerUrl;
	}

	public static String getControllerCert() {
		return controllerCert;
	}

	public static String getNetworkInterface() {
		return networkInterface;
	}

	public static String getDockerUrl() {
		return dockerUrl;
	}

	public static float getDiskLimit() {
		return diskLimit;
	}

	public static float getMemoryLimit() {
		return memoryLimit;
	}

	public static String getDiskDirectory() {
		return diskDirectory;
	}

	public static float getCpuLimit() {
		return cpuLimit;
	}

	public static String getInstanceId() {
		return instanceId;
	}

	public static int getLogFileCount() {
		return logFileCount;
	}

	public static float getLogDiskLimit() {
		return logDiskLimit;
	}

	public static String getLogDiskDirectory() {
		return logDiskDirectory;
	}

	public static void setLogDiskDirectory(String logDiskDirectory) {
		Configuration.logDiskDirectory = logDiskDirectory;
	}

	public static void setAccessToken(String accessToken) {
		try {
			setNode("access_token", accessToken);
		} catch (Exception e){}
		Configuration.accessToken = accessToken;
	}

	public static void setInstanceId(String instanceId) {
		try {
			setNode("instance_id", instanceId);
		} catch (Exception e){}
		Configuration.instanceId = instanceId;
	}

	public static void setControllerUrl(String controllerUrl) {
		Configuration.controllerUrl = controllerUrl;
	}

	public static void setControllerCert(String controllerCert) {
		Configuration.controllerCert = controllerCert;
	}

	public static void setNetworkInterface(String networkInterface) {
		Configuration.networkInterface = networkInterface;
	}

	public static void setDockerUrl(String dockerUrl) {
		Configuration.dockerUrl = dockerUrl;
	}

	public static void setDiskLimit(float diskLimit) throws Exception {
		if(diskLimit < 1 || diskLimit > 1048576) throw new Exception("Disk limit range must be 1 to 1048576 GB");
		Configuration.diskLimit = diskLimit;
	}

	public static void setMemoryLimit(float memoryLimit) throws Exception {
		if(memoryLimit < 128 || memoryLimit > 1048576) throw new Exception("Memory limit range must be 128 to 1048576 MB");
		Configuration.memoryLimit = memoryLimit;
	}

	public static void setDiskDirectory(String diskDirectory) {
		if (diskDirectory.charAt(diskDirectory.length() - 1) != File.separatorChar)
			diskDirectory += File.separatorChar;
		Configuration.diskDirectory = diskDirectory;
	}

	public static void setCpuLimit(float cpuLimit) throws Exception {
		if(cpuLimit < 5 || cpuLimit > 100) throw new Exception("CPU limit range must be 5% to 100%");
		Configuration.cpuLimit = cpuLimit;
	}

	public static void setLogDiskLimit(float logDiskLimit) throws Exception {
		if(logDiskLimit < 0.5 || logDiskLimit > 1024) throw new Exception("Log disk limit range must be 0.5 to 1024 GB");
		Configuration.logDiskLimit = logDiskLimit;
	}

	public static void setLogFileCount(int logFileCount) throws Exception {
		if(logFileCount < 1 || logFileCount > 100) throw new Exception("Log file count range must be 1 to 100 ");
		Configuration.logFileCount = logFileCount;
	}

	/**
	 * returns report for "info" commandline parameter
	 * 
	 * @return info report
	 */
	public static String getConfigReport() {
		String ipAddress;
		try {
			ipAddress = Orchestrator.getInetAddress().getHostAddress();
		} catch (Exception e) {
			ipAddress = "unable to retrieve ip address";
		}

		StringBuilder result = new StringBuilder();
		result.append(
				"Instance ID               : " + ((instanceId != null && !instanceId.equals("")) ? instanceId : "not provisioned") + "\n" + 
						"IP Address                : " + ipAddress + "\n" + 
						"Network Adapter           : " + networkInterface + "\n" + 
						"ioFabric Controller       : " + controllerUrl + "\n" + 
						"ioFabric Certificate      : " + controllerCert + "\n" + 
						"Docker URI                : " + dockerUrl + "\n" + 
						String.format("Disk Limit                : %.2f GiB\n", diskLimit) + 
						"Disk Directory            : " + diskDirectory + "\n" + 
						String.format("Memory Limit              : %.2f MiB\n", memoryLimit) + 
						String.format("CPU Limit                 : %.2f%%\n", cpuLimit) + 
						String.format("Log Limit                 : %.2f GiB\n", logDiskLimit) + 
						"Log Directory             : " + logDiskDirectory + "\n" + 
						String.format("Log File Count            : %d", logFileCount));
		return result.toString();
	}

}