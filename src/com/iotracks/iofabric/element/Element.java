package com.iotracks.iofabric.element;

import java.util.List;

public class Element {
	private final String elementId;
	private final String imageName;
	private String elementConfig;
	private List<PortMapping> portMappings;
	private long startTime;
	private long lastModified;
	private long lastUpdated;
	private Route route;
	private String containerId;
	private Registry registry;
	private String containerIpAddress;
	
	public String getContainerIpAddress() {
		return containerIpAddress;
	}

	public void setContainerIpAddress(String containerIpAddress) {
		this.containerIpAddress = containerIpAddress;
	}

	public Registry getRegistry() {
		return registry;
	}

	public void setRegistry(Registry registry) {
		this.registry = registry;
	}

	public String getContainerId() {
		return containerId;
	}

	public void setContainerId(String containerId) {
		this.containerId = containerId;
	}

	public Element(String elementId, String imageName) {
		this.elementId = elementId;
		this.imageName = imageName;
		containerId = "";
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

	public String getElementId() {
		return elementId;
	}

	public String getImageName() {
		return imageName;
	}
	
	@Override
	public String toString() {
		return "{\n" + 
				"\"elementId\":\"" + this.elementId + "\",\n" + 
				"\"imageName\":\"" + this.imageName + "\",\n" + 
				"\"lastModified\":\"" + this.lastModified + "\",\n" + 
				"\"lastUpdateed\":\"" + this.lastUpdated + "\",\n" + 
				"\"elementConfig\":\"" + this.elementConfig + "\",\n" + 
				"\"startTime\":\"" + this.startTime + "\",\n" + 
				"\"portMappings\":\"" + ((this.portMappings != null) ? this.portMappings.toString() : "[]") + "\",\n" + 
				"\"routes\":\"" + (this.route != null ? this.route.toString() : "{}") + "\"\n" + 
				"}";
	}

	public long getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(long lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
	
	@Override
	public boolean equals(Object e) {
		if (e == null)
			return false;
		Element element = (Element) e;
		return this.elementId.equals(element.getElementId());
//		return  this.elementId.equals(element.getElementId()) &&
//				this.imageName.equals(element.getImageName()) &&
//				this.elementConfig.equals(element.getElementConfig()) &&
//				this.startTime == element.getStartTime() &&
//				this.lastModified == element.getLastModified() &&
//				this.lastUpdated == element.getLastUpdated() &&
//				this.route.equals(element.getRoute()) &&
//				this.portMappings.equals(element.getPortMappings());
	}
	
}
