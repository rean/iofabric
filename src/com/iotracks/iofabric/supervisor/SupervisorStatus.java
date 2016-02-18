package com.iotracks.iofabric.supervisor;

import com.iotracks.iofabric.utils.Constants;

public class SupervisorStatus {
	public enum StatusEnum {
		starting, running, stopped
	}
	private StatusEnum daemonStatus;			// FC
	private StatusEnum[] modulesStatus;
	private long daemonLastStart;				// FC
	private long operationDuration;				// FC
	
	
	public SupervisorStatus() {
		modulesStatus = new StatusEnum[Constants.NUMBER_OF_MODULES];
		for (int i = 0; i < Constants.NUMBER_OF_MODULES; i++)
			modulesStatus[i] = StatusEnum.starting;
	}

	public SupervisorStatus setModuleStatus(int module, StatusEnum status) {
		modulesStatus[module] = status;
		return this;
	}
	
	public StatusEnum getModuleStatus(int module) {
		return modulesStatus[module];
	}
	
	public StatusEnum getDaemonStatus() {
		return daemonStatus;
	}
	
	public SupervisorStatus setDaemonStatus(StatusEnum daemonStatus) {
		this.daemonStatus = daemonStatus;
		return this;
	}
	
	public long getDaemonLastStart() {
		return daemonLastStart;
	}
	
	public SupervisorStatus setDaemonLastStart(long daemonLastStart) {
		this.daemonLastStart = daemonLastStart;
		return this;
	}
	
	public long getOperationDuration() {
		return operationDuration - daemonLastStart;
	}
	
	public SupervisorStatus setOperationDuration(long operationDuration) {
		this.operationDuration = operationDuration;
		return this;
	}
}
