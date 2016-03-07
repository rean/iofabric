package com.iotracks.iofabric.element;

import java.util.List;

public class Element {
	private final String elementId;
	private final String imageName;
	private List<PortMapping> portMappings;
	private long startTime;
	private long lastModified;
	private long lastUpdated;
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

	public String getElementId() {
		return elementId;
	}

	public String getImageName() {
		return imageName;
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
	}
	
}
