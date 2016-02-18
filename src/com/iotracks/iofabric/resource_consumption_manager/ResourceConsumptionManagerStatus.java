package com.iotracks.iofabric.resource_consumption_manager;

public class ResourceConsumptionManagerStatus {
	private float memoryUsage;				// FC
	private float diskUsage;				// FC
	private float cpuUsage;					// FC
	private boolean memoryViolation;		// FC
	private boolean diskViolation;			// FC
	private boolean cpuViolation;			// FC
	
	public ResourceConsumptionManagerStatus() {
	}
	
	public float getMemoryUsage() {
		return memoryUsage;
	}
	
	public ResourceConsumptionManagerStatus setMemoryUsage(float memoryUsage) {
		this.memoryUsage = memoryUsage;
		return this;
	}
	
	public float getDiskUsage() {
		return diskUsage;
	}
	
	public ResourceConsumptionManagerStatus setDiskUsage(float diskUsage) {
		this.diskUsage = diskUsage;
		return this;
	}
	
	public float getCpuUsage() {
		return cpuUsage;
	}
	
	public ResourceConsumptionManagerStatus setCpuUsage(float cpuUsage) {
		this.cpuUsage = cpuUsage;
		return this;
	}
	
	public boolean isDiskViolation() {
		return diskViolation;
	}
	
	public ResourceConsumptionManagerStatus setDiskViolation(boolean diskViolation) {
		this.diskViolation = diskViolation;
		return this;
	}
	
	public boolean isCpuViolation() {
		return cpuViolation;
	}
	
	public ResourceConsumptionManagerStatus setCpuViolation(boolean cpViolation) {
		this.cpuViolation = cpViolation;
		return this;
	}
	
	public boolean isMemoryViolation() {
		return memoryViolation;
	}

	public ResourceConsumptionManagerStatus setMemoryViolation(boolean memoryViolation) {
		this.memoryViolation = memoryViolation;
		return this;
	}
	
}
