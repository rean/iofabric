package com.iotracks.iofabric.element;

import com.iotracks.iofabric.utils.Constants;

/**
 * Class represents IOElement status
 * 
 * @author saeid
 *
 */
public class ElementStatus {

	private Constants.ElementState status;
	private long startTime;
	private float cpuUsage;
	private long memoryUsage;

	public float getCpuUsage() {
		return cpuUsage;
	}

	public void setCpuUsage(float cpuUsage) {
		this.cpuUsage = cpuUsage;
	}

	public long getMemoryUsage() {
		return memoryUsage;
	}

	public void setMemoryUsage(long memoryUsage) {
		this.memoryUsage = memoryUsage;
	}

	public com.iotracks.iofabric.utils.Constants.ElementState getStatus() {
		return status;
	}

	public void setStatus(com.iotracks.iofabric.utils.Constants.ElementState status) {
		this.status = status;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getOperatingDuration() {
		return System.currentTimeMillis() - startTime;
	}

}
