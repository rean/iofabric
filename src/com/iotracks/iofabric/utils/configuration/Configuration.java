package com.iotracks.iofabric.utils.configuration;

import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Map;

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
	
	public static boolean configChanged;
	public static boolean debugging = true;

	private static String getNode(String name) throws ConfigurationItemException {
		NodeList nodes = configElement.getElementsByTagName(name);
		if (nodes.getLength() != 1)
			throw new ConfigurationItemException("<" + name + "> item not found or defined more than once");

		return nodes.item(0).getTextContent();
	}

	private static void setNode(String name, String content) throws ConfigurationItemException {

		NodeList nodes = configFile.getElementsByTagName(name);

		if (nodes.getLength() != 1)
			throw new ConfigurationItemException("<" + name + "> item not found or defined more than once");

		nodes.item(0).setTextContent(content);

	}
	
	private static void saveConfigUpdates() throws Exception {
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		StreamResult result = new StreamResult(new File("/etc/iofabric/config.xml"));
		DOMSource source = new DOMSource(configFile);
		transformer.transform(source, result);
	}

	public static void setConfig(Map<String, String> commandLineMap) throws Exception {
		String option = null, value = null;

		for (Map.Entry<String, String> command : commandLineMap.entrySet()) {
			option = command.getKey();
			value = command.getValue();
			
			if(option == null || value == null || value.trim() == "" || option.trim() == ""){
				throw new ConfigurationItemException("Command or value is invalid");
			}
			
			switch (option) {
			case "d":
				validateValue(option, value, "isPositiveFloat");
				setNode("disk_consumption_limit", value);
				setDiskLimit(Float.parseFloat(value));
				break;
			case "dl":
				setNode("disk_directory", value);
				setDiskDirectory(value);
				break;
			case "m":
				validateValue(option, value, "isPositiveFloat");
				setNode("memory_consumption_limit", value);
				setMemoryLimit(Float.parseFloat(value));
				break;
			case "p":
				validateValue(option, value, "isPositiveFloat");
				setNode("processor_consumption_limit", value);
				setCpuLimit(Float.parseFloat(value));
				break;
			case "a":
				setNode("controller_url", value);
				setControllerUrl(value);
				break;
			case "ac":
				setNode("controller_cert", value);
				setControllerCert(value);
				break;
			case "c":
				setNode("docker_url", value);
				setDockerUrl(value);
				break;
			case "n":
				setNode("network_interface", value);
				setNetworkInterface(value);
				break;
			case "l":
				validateValue(option, value, "isPositiveFloat");
				setNode("log_disk_consumption_limit", value);
				setLogDiskLimit(Float.parseFloat(value));
				break;
			case "ld":
				setNode("log_disk_directory", value);
				setLogDiskDirectory(value);
				break;
			case "lc":
				validateValue(option, value, "isPositiveInteger");
				setNode("log_file_count", value);
				setLogFileCount(Integer.parseInt(value));
				break;
			default:
				throw new ConfigurationItemException("-" + option + " : Command not found");
			}

		}
		
		saveConfigUpdates();
		configChanged = true;
	}

	private static void validateValue(String option, String value, String typeOfValidation) throws ConfigurationItemException {
		if(typeOfValidation == "isPositiveFloat"){
			if (!value.matches("[0-9]*.?[0-9]*"))
				throw new ConfigurationItemException("Option -" + option + " has invalid value: " + value);
		}else if(typeOfValidation == "isPositiveInteger"){
			if (!value.matches("[0-9]*"))
				throw new ConfigurationItemException("Option -" + option + " has invalid value: " + value);
		}
	}
	
	public static void loadConfig() throws Exception {
		// TODO: load configuration XML file here
		configChanged = false;
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
		Configuration.accessToken = accessToken;
	}

	public static void setInstanceId(String instanceId) {
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

	public static void setDiskLimit(float diskLimit) {
		Configuration.diskLimit = diskLimit;
	}

	public static void setMemoryLimit(float memoryLimit) {
		Configuration.memoryLimit = memoryLimit;
	}

	public static void setDiskDirectory(String diskDirectory) {
		if (diskDirectory.charAt(diskDirectory.length() - 1) != File.separatorChar)
			diskDirectory += File.separatorChar;
		Configuration.diskDirectory = diskDirectory;
	}

	public static void setCpuLimit(float cpuLimit) {
		Configuration.cpuLimit = cpuLimit;
	}

	public static void setLogDiskLimit(float logDiskLimit) {
		Configuration.logDiskLimit = logDiskLimit;
	}

	public static void setLogFileCount(int logFileCount) {
		Configuration.logFileCount = logFileCount;
	}

	public static String getConfigReport() {
		String ipAddress = "";
		try {
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
		    while (networkInterfaces.hasMoreElements()) {
		        NetworkInterface networkInterface = networkInterfaces.nextElement();
		        if (networkInterface.getName().equals(Configuration.networkInterface)) {
		        	Enumeration<InetAddress> ipAddresses = networkInterface.getInetAddresses();
		        	while (ipAddresses.hasMoreElements()) {
		        		InetAddress address = ipAddresses.nextElement();
		        		if (address instanceof Inet4Address) {
		        			ipAddress = address.toString().substring(1);
		        			break;
		        		}	
		        	}
		        	if (!ipAddress.equals(""))
		        		break;
		        }
		    }
		} catch (SocketException e) {}
		
		if (ipAddress.equals(""))
			ipAddress = "unable to retrieve ip address";

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