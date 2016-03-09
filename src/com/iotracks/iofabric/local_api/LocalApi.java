package com.iotracks.iofabric.local_api;

import java.util.Map;

import com.iotracks.iofabric.element.ElementManager;
import com.iotracks.iofabric.status_reporter.StatusReporter;

public class LocalApi {

	private final String MODULE_NAME = "Local API";

	public LocalApi() {

	}

	public static void main(String[] args) throws Exception {
	//	ElementManager elementManager = new ElementManager();
	//	elementManager.loadFromApi();
		LocalApi api = new LocalApi();
		api.start();
	}

	public void start() throws Exception {
		ConfigurationMap configMap = ConfigurationMap.getInstance();
		retrieveContainerConfig();
		WebSocketMap socketMap = WebSocketMap.getInstance();
		LocalApiServer server = new LocalApiServer();
		server.start();
		StatusReporter.setLocalApiStatus().setCurrentIpAddress("127.0.0.1");
	}

	public void retrieveContainerConfig() throws Exception{
		ConfigurationMap.containerConfigMap = ElementManager.getConfigs();	  
	}

	public void updateContainerConfig(String containerId, String config) throws Exception{
		for(Map.Entry<String, String> entry : ConfigurationMap.containerConfigMap.entrySet()){
			if(entry.getKey().equals(containerId)){
				entry.setValue(config);
			}
		}
	}
}
