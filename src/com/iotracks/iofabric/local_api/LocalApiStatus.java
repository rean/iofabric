package com.iotracks.iofabric.local_api;

public class LocalApiStatus {
	private String currentIpAddress;
	private int openConfigSocketsCount;
	private int openMessageSocketsCount;
	
	public String getCurrentIpAddress() {
		return currentIpAddress;
	}
	
	public LocalApiStatus setCurrentIpAddress(String currentIpAddress) {
		this.currentIpAddress = currentIpAddress;
		return this;
	}
	
	public int getOpenConfigSocketsCount() {
		return openConfigSocketsCount;
	}
	
	public LocalApiStatus setOpenConfigSocketsCount(int openConfigSocketsCount) {
		this.openConfigSocketsCount = openConfigSocketsCount;
		return this;
	}
	
	public int getOpenMessageSocketsCount() {
		return openMessageSocketsCount;
	}
	
	public LocalApiStatus setOpenMessageSocketsCount(int openMessageSocketsCount) {
		this.openMessageSocketsCount = openMessageSocketsCount;
		return this;
	}
}
