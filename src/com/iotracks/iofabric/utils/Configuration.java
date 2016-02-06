package com.iotracks.iofabric.utils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Configuration {

	private static Configuration instance = null;

	private String accessToken;
	private String controllerUrl;
	private String controllerCert;
	private String networkInterface;
	private String dockerUrl;
	private float diskLimit; // in GiB
	private float memoryLimit; // in MiB
	private String diskDirectory;
	private float cpuLimit;
	private float logDiskLimit;
	private String logDiskDirectory;

	public float getLogDiskLimit() {
		return logDiskLimit;
	}

	public String getLogDiskDirectory() {
		return logDiskDirectory;
	}

	private Configuration() throws Exception {
		// TODO: load configuration XML file here
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		Document configFile = builder.parse("config/config.xml");
		configFile.getDocumentElement().normalize();
		Element configElement = (Element) configFile.getElementsByTagName("config").item(0);
		
		accessToken = configElement.getElementsByTagName("access_token").item(0).getTextContent();
		
		controllerUrl = configElement.getElementsByTagName("controller_url").item(0).getTextContent();
		
	    controllerCert = configElement.getElementsByTagName("controller_cert").item(0).getTextContent();
	    
		networkInterface = configElement.getElementsByTagName("network_interface").item(0).getTextContent();
		
		dockerUrl = configElement.getElementsByTagName("docker_url").item(0).getTextContent();
		
		diskLimit = Float.parseFloat(configElement.getElementsByTagName("disk_consumption_limit").item(0).getTextContent());
		
		diskDirectory = configElement.getElementsByTagName("disk_directory").item(0).getTextContent();
		
		memoryLimit = Float.parseFloat(configElement.getElementsByTagName("memory_consumption_limit").item(0).getTextContent());
		
		cpuLimit = Float.parseFloat(configElement.getElementsByTagName("processor_consumption_limit").item(0).getTextContent());
		
		logDiskDirectory = configElement.getElementsByTagName("log_disk_directory").item(0).getTextContent();
		
		logDiskLimit = Float.parseFloat(configElement.getElementsByTagName("log_disk_consumption_limit").item(0).getTextContent());
		
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

}
