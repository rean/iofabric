package com.iotracks.iofabric.utils;

// Singleton
// each module reports it's last active time to the class
public class ModulesActivity {

	public static ModulesActivity instance = null;
	private long[] modulesLastActiveTime;
	
	
	private ModulesActivity() {
		modulesLastActiveTime = new long[Constants.NUMBER_OF_MODULES];
	}

	
	public static ModulesActivity getInstance() {
		if (instance == null) {
			synchronized (ModulesActivity.class) {
				if (instance == null) {
					instance = new ModulesActivity();
				}
			}
		}
		
		return instance;
	}
	
	
	public long getModuleLastActiveTime(int module) {
		return modulesLastActiveTime[module];
	}

	public void setModuleLastActiveTime(int module) {
		modulesLastActiveTime[module] = System.currentTimeMillis();
	}

}
