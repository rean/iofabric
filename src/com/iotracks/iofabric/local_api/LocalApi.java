package com.iotracks.iofabric.local_api;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import com.iotracks.iofabric.element.ElementManager;
import com.iotracks.iofabric.message_bus.MessageBus;
import com.iotracks.iofabric.status_reporter.StatusReporter;
import com.iotracks.iofabric.utils.Constants;
import com.iotracks.iofabric.utils.Constants.ModulesStatus;
import com.iotracks.iofabric.utils.Observer;
import com.iotracks.iofabric.utils.configuration.Configuration;
import com.iotracks.iofabric.utils.logging.LoggingService;

public class LocalApi implements Observer{

	private final String MODULE_NAME = "Local API";
	private static LocalApi instance = null;

	private LocalApi() {

	}

	public static LocalApi getInstance(){
		if (instance == null) {
			synchronized (LocalApi.class) {
				if(instance == null){
					instance = new LocalApi();
					LoggingService.logInfo("LOCAL API ","Local Api Instantiated");
				}
			}
		}
		return instance;
	}

	public static void main(String[] args) throws Exception {
		Configuration.loadConfig();
		ElementManager.getInstance().loadFromApi();
		LoggingService.logInfo("Main", "configuration loaded.");
		MessageBus messageBus = MessageBus.getInstance();
		LocalApi api = LocalApi.getInstance();
		api.start();
	}

	public void start() throws Exception {
		StatusReporter.setSupervisorStatus().setModuleStatus(Constants.LOCAL_API, ModulesStatus.STARTING);
		
		WebSocketMap socketMap = WebSocketMap.getInstance();
		ConfigurationMap configMap = ConfigurationMap.getInstance();
		LoggingService.logInfo("Main", "Initialized configuration and websocket map");

		StatusReporter.setLocalApiStatus().setCurrentIpAddress(getCurrentIp());
		StatusReporter.setLocalApiStatus().setOpenConfigSocketsCount(WebSocketMap.controlWebsocketMap.size());
		StatusReporter.setLocalApiStatus().setOpenMessageSocketsCount(WebSocketMap.messageWebsocketMap.size());

		retrieveContainerConfig();

		LocalApiServer server = new LocalApiServer();
		try {
			server.start();
		} catch (Exception e) {
			try {
				server.stop();
			} catch (Exception e1) {
				LoggingService.logWarning(MODULE_NAME, "unable to start local api server: " + e1.getMessage());
				StatusReporter.setSupervisorStatus().setModuleStatus(Constants.LOCAL_API, ModulesStatus.STOPPED);
				return;
			}

			LoggingService.logWarning(MODULE_NAME, "unable to start local api server: " + e.getMessage());
			StatusReporter.setSupervisorStatus().setModuleStatus(Constants.LOCAL_API, ModulesStatus.STOPPED);
			return;
		}

	}

	public void retrieveContainerConfig() {
		try {
			ConfigurationMap.containerConfigMap = ElementManager.getInstance().getConfigs();
			LoggingService.logInfo(MODULE_NAME, "Container configuration retrieved");
		} catch (Exception e) {
			LoggingService.logWarning(MODULE_NAME, "unable to retrieve containers configuration: " + e.getMessage());
		}	  
	}

	public void updateContainerConfig(){
		try {
			ConfigurationMap.containerConfigMap = ElementManager.getInstance().getConfigs();
			LoggingService.logInfo(MODULE_NAME, "Container configuration updated");
		} catch (Exception e) {
			LoggingService.logWarning(MODULE_NAME, "unable to update containers configuration: " + e.getMessage());
		}
	}

	private InetAddress getCurrentIp(){
		InetAddress IP = null;
		try {
			IP = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			LoggingService.logWarning(MODULE_NAME, "unable to find the current IP");
		}
		LoggingService.logInfo(MODULE_NAME, "IP address of the system running iofabric is := " + IP.getHostAddress());
		return IP;
	}

	@Override
	public void update() {
		Map<String, String> oldConfigMap = new HashMap<String, String>();
		oldConfigMap.putAll(ConfigurationMap.containerConfigMap);
		updateContainerConfig();
		Map<String, String> newConfigMap = new HashMap<String, String>();
		newConfigMap.putAll(ConfigurationMap.containerConfigMap);
		ControlWebsocketHandler handler = new ControlWebsocketHandler();
		handler.initiateControlSignal(oldConfigMap, newConfigMap);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}