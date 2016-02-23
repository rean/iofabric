package com.iotracks.iofabric.element;

import java.util.List;

public class Element {
	private final String elementID;
	private final String imageName;
	private String elementConfig;
	private List<PortMapping> portMappings;
	private long startTime;
	private long lastModified;
	private Route route;
	private String containerID;
	
	public String getContainerID() {
		return containerID;
	}

	public void setContainerID(String containerID) {
		this.containerID = containerID;
	}

	public Element(String elementID, String imageName) {
		this.elementID = elementID;
		this.imageName = imageName;
	}

	public String getElementConfig() {
		return elementConfig;
	}

	public void setElementConfig(String elementConfig) {
		this.elementConfig = elementConfig;
	}

	public List<PortMapping> getPortMappings() {
		return portMappings;
	}

	public void setPortMappings(List<PortMapping> portMappings) {
		this.portMappings = portMappings;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getLastModified() {
		return lastModified;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	public Route getRoute() {
		return route;
	}

	public void setRoute(Route route) {
		this.route = route;
	}

	public String getElementID() {
		return elementID;
	}

	public String getImageName() {
		return imageName;
	}
	
	@Override
	public String toString() {
		return "{\n" + 
				"\"elementID\":\"" + this.elementID + "\",\n" + 
				"\"imageName\":\"" + this.imageName + "\",\n" + 
				"\"lastModified\":\"" + this.lastModified + "\",\n" + 
				"\"elementConfig\":\"" + this.elementConfig + "\",\n" + 
				"\"startTime\":\"" + this.startTime + "\",\n" + 
				"\"portMappings\":\"" + ((this.portMappings != null) ? this.portMappings.toString() : "[]") + "\",\n" + 
				"\"routes\":\"" + (this.route != null ? this.route.toString() : "{}") + "\"\n" + 
				"}";
	}
	
}
