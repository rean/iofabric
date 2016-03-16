package com.iotracks.iofabric.field_agent;

import com.iotracks.iofabric.utils.Constants;

public class FieldAgentStatus {

	private Constants.ControllerStatus contollerStatus;
	private long lastCommandTime;
	private boolean controllerVerified;

	public Constants.ControllerStatus getContollerStatus() {
		return contollerStatus;
	}

	public void setContollerStatus(Constants.ControllerStatus contollerStatus) {
		this.contollerStatus = contollerStatus;
	}

	public long getLastCommandTime() {
		return lastCommandTime;
	}

	public void setLastCommandTime(long lastCommandTime) {
		this.lastCommandTime = lastCommandTime;
	}

	public boolean isControllerVerified() {
		return controllerVerified;
	}

	public void setControllerVerified(boolean controllerVerified) {
		this.controllerVerified = controllerVerified;
	}

}
