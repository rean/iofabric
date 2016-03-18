package com.iotracks.iofabric.element;

public class ElementStatus {

	private com.iotracks.iofabric.utils.Constants.ElementStatus status;
	private long startTime;

	public com.iotracks.iofabric.utils.Constants.ElementStatus getStatus() {
		return status;
	}

	public void setStatus(com.iotracks.iofabric.utils.Constants.ElementStatus status) {
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
