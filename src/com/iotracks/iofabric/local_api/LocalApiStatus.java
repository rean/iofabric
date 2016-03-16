package com.iotracks.iofabric.local_api;

import java.net.InetAddress;

public class LocalApiStatus {
	private InetAddress currentIpAddress;
	private int openConfigSocketsCount;
	private int openMessageSocketsCount;
	
	public InetAddress getCurrentIpAddress() {
		return currentIpAddress;
	}
	
	public LocalApiStatus setCurrentIpAddress(InetAddress currentIpAddress) {
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
