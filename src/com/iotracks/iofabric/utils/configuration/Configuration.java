package com.iotracks.iofabric.utils.configuration;

import java.io.File;
import java.util.HashMap;
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

public class Configuration {

	private static Configuration instance = null;
	private Element configElement;
	private Document configFile;

	private String accessToken;
	private String instanceId;
	private String controllerUrl;
	private String controllerCert;
	private String networkInterface;
	private String dockerUrl;
	private float diskLimit;
	private float memoryLimit;
	private String diskDirectory;
	private float cpuLimit;
	private float logDiskLimit;
	private String logDiskDirectory;
	private int logFileCount;

	private String getNode(String name) throws ConfigurationItemException {
		NodeList nodes = configElement.getElementsByTagName(name);
		
		if (nodes.getLength() != 1)
			throw new ConfigurationItemException("<" + name + "> item not found or defined more than once");
		
		return nodes.item(0).getTextContent();
	}
	
	private void setNode(String name, String content) throws Exception {
	
		NodeList nodes = configFile.getElementsByTagName(name);
		
		if (nodes.getLength() != 1)
			throw new ConfigurationItemException("<" + name + "> item not found or defined more than once");
		
		nodes.item(0).setTextContent(content);
		
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		StreamResult result = new StreamResult(new File("/etc/iofabric/config.xml"));
		DOMSource source = new DOMSource(configFile);
		transformer.transform(source, result);

	}
	
	private void setConfig(HashMap<String, String> commandLineMap) throws Exception{
		String option = null, value = null;

		for(Map.Entry<String, String> command : commandLineMap.entrySet()){
			option = command.getKey();
			value = command.getValue();
			
			switch(option){
				case "d": 
					validatePositiveFloatValue(option, value);
					setNode("disk_consumption_limit", value);
					setDiskLimit(Float.parseFloat(value));
					break;
				case "dl":
					setNode("disk_directory", value);
					setDiskDirectory(value);
					break;
				case "m":
					validatePositiveFloatValue(option, value);
					setNode("memory_consumption_limit", value);
					setMemoryLimit(Float.parseFloat(value));
					break;
				case "p":
					validatePositiveFloatValue(option, value);
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
					validatePositiveFloatValue(option, value);
					setNode("log_disk_consumption_limit", value);
					setLogDiskLimit(Float.parseFloat(value));
					break;
				case "ld":
					setNode("log_disk_directory", value);
					setLogDiskDirectory(value);
					break;
				case "lc":
					validatePositiveIntegerValue(option, value);
					setNode("log_file_count", value);
					setLogFileCount(Integer.parseInt(value));
					break;
				default:
						throw new ConfigurationItemException("-" + option + " : Command not found");
			}
			
		}
		
	}
	
	private void validatePositiveFloatValue (String option, String value) throws ConfigurationItemException{
		if (!value.matches("[0-9]*.?[0-9]*")) 
			throw new ConfigurationItemException("Option -" + option + " has invalid value: " + value);
	}
	
	private void validatePositiveIntegerValue (String option, String value) throws ConfigurationItemException{
		if (!value.matches("[0-9]*")) 
			throw new ConfigurationItemException("Option -" + option + " has invalid value: " + value);
	}	

	private Configuration() throws Exception {
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
		
		instanceId = getNode("instance_id"); 
		accessToken = getNode("access_token");
		controllerUrl = getNode("controller_url");
	    controllerCert = getNode("controller_cert");
		networkInterface = getNode("network_interface");
		dockerUrl = getNode("docker_url");
		diskLimit = Float.parseFloat(getNode("disk_consumption_limit"));
		diskDirectory = getNode("disk_directory");
		memoryLimit = Float.parseFloat(getNode("memory_consumption_limit"));
		cpuLimit = Float.parseFloat(getNode("processor_consumption_limit"));
		logDiskDirectory = getNode("log_disk_directory");
		logDiskLimit = Float.parseFloat(getNode("log_disk_consumption_limit"));
		logFileCount = Integer.parseInt(configElement.getElementsByTagName("log_file_count").item(0).getTextContent());
		
	}

	// Singleton
	public static Configuration getInstance() throws Exception {
		if (instance == null) {
			synchronized (Configuration.class) {
				if (instance == null) {
					instance = new Configuration();
				}
			}
		}

		return instance;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public String getControllerUrl() {
		return controllerUrl;
	}

	public String getControllerCert() {
		return controllerCert;
	}

	public String getNetworkInterface() {
		return networkInterface;
	}

	public String getDockerUrl() {
		return dockerUrl;
	}

	public float getDiskLimit() {
		return diskLimit;
	}

	public float getMemoryLimit() {
		return memoryLimit;
	}

	public String getDiskDirectory() {
		return diskDirectory;
	}

	public float getCpuLimit() {
		return cpuLimit;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public int getLogFileCount() {
		return logFileCount;
	}

	public float getLogDiskLimit() {
		return logDiskLimit;
	}

	public String getLogDiskDirectory() {
		return logDiskDirectory;
	}
	
	public void setLogDiskDirectory(String logDiskDirectory) {
		this.logDiskDirectory = logDiskDirectory;
	}
	
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public void setControllerUrl(String controllerUrl) {
		this.controllerUrl = controllerUrl;
	}

	public void setControllerCert(String controllerCert) {
		this.controllerCert = controllerCert;
	}

	public void setNetworkInterface(String networkInterface) {
		this.networkInterface = networkInterface;
	}

	public void setDockerUrl(String dockerUrl) {
		this.dockerUrl = dockerUrl;
	}

	public void setDiskLimit(float diskLimit) {
		this.diskLimit = diskLimit;
	}

	public void setMemoryLimit(float memoryLimit) {
		this.memoryLimit = memoryLimit;
	}

	public void setDiskDirectory(String diskDirectory) {
		this.diskDirectory = diskDirectory;
	}

	public void setCpuLimit(float cpuLimit) {
		this.cpuLimit = cpuLimit;
	}

	public void setLogDiskLimit(float logDiskLimit) {
		this.logDiskLimit = logDiskLimit;
	}

	public void setLogFileCount(int logFileCount) {
		this.logFileCount = logFileCount;
	}

}