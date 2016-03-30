package com.iotracks.iofabric.process_manager;

import com.github.dockerjava.api.model.Container;
import com.iotracks.iofabric.element.Element;
import com.iotracks.iofabric.element.ElementManager;
import com.iotracks.iofabric.element.ElementStatus;

public class FabricContainer {
	private String containerId;
	private final String elementId;
	private ElementStatus status;
	DockerUtil docker;
	
	public FabricContainer(String elementId) {
		this.elementId = elementId;
	}
	
	private final Runnable checkStatus = () -> {
		//
	};
	
	public void start() {
		
	}
	
	public void stop() {
		
	}
	
	public void create() {
		
	}
	
	public void remove() {
		
	}
	
	public void init() {
		docker = DockerUtil.getInstance();
		try {
			docker.connect();
		} catch (Exception e) {}
		Element element = ElementManager.getInstance().getElementById(elementId);
		Container container = docker.getContainer(elementId);
		if (container != null) {
			containerId = container.getId();
			element.setContainerId(containerId);
//			status = docker.getContainerStatus(containerId);
		} else
			containerId = null;
	}
}
