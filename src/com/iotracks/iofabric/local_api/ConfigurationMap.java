package com.iotracks.iofabric.local_api;

import java.util.HashMap;
import java.util.Map;

public class ConfigurationMap {
	static Map<String, String> containerConfigMap;

	private static ConfigurationMap instance = null;

	private ConfigurationMap(){

	}

	public static ConfigurationMap getInstance(){
		if(instance == null){
			instance = new ConfigurationMap();
			containerConfigMap = new HashMap<String, String>();
		}
		
		return instance;
	}
}
