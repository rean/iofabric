package com.iotracks.iofabric.local_api;

import java.util.Map;

import com.iotracks.iofabric.element.ElementManager;
import com.iotracks.iofabric.status_reporter.StatusReporter;
import com.iotracks.iofabric.utils.Constants;
import com.iotracks.iofabric.utils.Constants.ModulesStatus;
import com.iotracks.iofabric.utils.logging.LoggingService;

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
		StatusReporter.setSupervisorStatus().setModuleStatus(Constants.LOCAL_API, ModulesStatus.STARTING);
		
		WebSocketMap socketMap = WebSocketMap.getInstance();
		ConfigurationMap configMap = ConfigurationMap.getInstance();
		retrieveContainerConfig();
		
		StatusReporter.setLocalApiStatus().setCurrentIpAddress("127.0.0.1");
		StatusReporter.setLocalApiStatus().setOpenConfigSocketsCount(WebSocketMap.controlWebsocketMap.size());
		StatusReporter.setLocalApiStatus().setOpenMessageSocketsCount(WebSocketMap.messageWebsocketMap.size());
		
		LoggingService.logInfo(MODULE_NAME, "Local api up");
		
		LocalApiServer server = new LocalApiServer();
		try {
			server.start();
		} catch (Exception e) {
			try {
				server.stop();
			} catch (Exception e1) {
				LoggingService.logWarning(MODULE_NAME, "unable to start local api server\n" + e1.getMessage());
			}
			
			LoggingService.logWarning(MODULE_NAME, "unable to start local api server\n" + e.getMessage());
			StatusReporter.setSupervisorStatus().setModuleStatus(Constants.LOCAL_API, ModulesStatus.STOPPED);
			return;
		}

	}

	public void retrieveContainerConfig() {
		try {
			ConfigurationMap.containerConfigMap = ElementManager.getConfigs();
		} catch (Exception e) {
			LoggingService.logWarning(MODULE_NAME, "unable to retrieve containers configuration\n" + e.getMessage());
		}	  
	}

	public void updateContainerConfig(String containerId, String config){
		try {
			for(Map.Entry<String, String> entry : ConfigurationMap.containerConfigMap.entrySet()){
				if(entry.getKey().equals(containerId)){
					entry.setValue(config);
				}
			}
		} catch (Exception e) {
			LoggingService.logWarning(MODULE_NAME, "unable to update containers configuration\n" + e.getMessage());
		}
	}
}
