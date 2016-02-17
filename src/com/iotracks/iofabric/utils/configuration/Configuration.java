package com.iotracks.iofabric.utils.configuration;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Configuration {

	private static Configuration instance = null;
	private Element configElement;

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
	

	private Configuration() throws Exception {
		// TODO: load configuration XML file here
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		Document configFile = builder.parse("/etc/iofabric/config.xml");
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

}