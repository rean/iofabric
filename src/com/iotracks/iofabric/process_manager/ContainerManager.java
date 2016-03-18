package com.iotracks.iofabric.process_manager;

import com.github.dockerjava.api.model.Container;
import com.iotracks.iofabric.element.Element;
import com.iotracks.iofabric.element.ElementManager;
import com.iotracks.iofabric.element.Registry;
import com.iotracks.iofabric.status_reporter.StatusReporter;
import com.iotracks.iofabric.utils.Constants.ElementStatus;
import com.iotracks.iofabric.utils.logging.LoggingService;

public class ContainerManager {

	private DockerUtil docker;
	private String containerId;
	private ContainerTask task;
	private ElementManager elementManager;

	private final String MODULE_NAME = "Container Manager";
	private final String IOFABRIC_HOST = "iofabric:127.0.0.1";

	public ContainerManager() {
		elementManager = ElementManager.getInstance();
	}
	
	private void addElement() throws Exception {
		Element element = (Element) task.data;

		try {
			Registry registry = elementManager.getRegistry(element.getRegistry());
			if (registry == null) {
				LoggingService.logWarning(MODULE_NAME, String.format("registry is not valid \"%s\"", element.getRegistry()));
				throw new Exception();
			}
			docker.login(registry);
		} catch (Exception e) {
			LoggingService.logWarning(MODULE_NAME, "docker login failed : " + e.getMessage());
			throw e;
		}

		StatusReporter.setProcessManagerStatus().getElementStatus(element.getElementId()).setStatus(ElementStatus.BUILDING);
		LoggingService.logInfo(MODULE_NAME, "building \"" + element.getImageName() + "\"");
		
		
		Container container = docker.getContainer(element.getElementId());
		if (container != null) {
			if (element.isRebuild()) {
				containerId = container.getId();
				try {
					stopContainer();
					removeContainer();
					docker.removeImage(element.getImageName());
				} catch (Exception e) {
					return;
				}
			} else
				return;
		}
		
		
		try {
			LoggingService.logInfo(MODULE_NAME, "pulling \"" + element.getImageName() + "\" from registry");
			docker.pullImage(element.getImageName());
			LoggingService.logInfo(MODULE_NAME, String.format("\"%s\" pulled", element.getImageName()));

			LoggingService.logInfo(MODULE_NAME, "creating container");
			String id = docker.createContainer(element, IOFABRIC_HOST);
			element.setContainerId(id);
			element.setContainerIpAddress(docker.getContainerIpAddress(id));
			element.setRebuild(false);
			LoggingService.logInfo(MODULE_NAME, "created");
		} catch (Exception ex) {
			LoggingService.logWarning(MODULE_NAME, ex.getMessage());
			StatusReporter.setProcessManagerStatus().getElementStatus(element.getElementId()).setStatus(ElementStatus.FAILED_VERIFICATION);
			throw ex;
		}
	}

	private void startElement() {
		Element element = (Element) task.data;
		StatusReporter.setProcessManagerStatus().getElementStatus(element.getElementId()).setStatus(ElementStatus.STARTING);
		LoggingService.logInfo(MODULE_NAME, String.format("starting container \"%s\"", element.getImageName()));
		try {
			docker.startContainer(element.getContainerId());
			LoggingService.logInfo(MODULE_NAME, String.format("\"%s\" started", element.getImageName()));
			StatusReporter.setProcessManagerStatus().getElementStatus(element.getElementId()).setStatus(ElementStatus.RUNNING);
		} catch (Exception ex) {
			LoggingService.logWarning(MODULE_NAME,
					String.format("container \"%s\" not found - %s", element.getImageName(), ex.getMessage()));
			StatusReporter.setProcessManagerStatus().getElementStatus(element.getElementId()).setStatus(ElementStatus.STOPPED);
		}
	}
	
	private void stopContainer() {
		LoggingService.logInfo(MODULE_NAME, String.format("stopping container \"%s\"", containerId));
		try {
			docker.stopContainer(containerId);
			LoggingService.logInfo(MODULE_NAME, String.format("container \"%s\" stopped", containerId));
		} catch (Exception e) {
			LoggingService.logWarning(MODULE_NAME, String.format("error stopping container \"%s\"", containerId));
		}
	}

	private void removeContainer() throws Exception {
		if (!docker.hasContainer(containerId))
			return;
		LoggingService.logInfo(MODULE_NAME, String.format("removing container \"%s\"", containerId));
		try {
			docker.removeContainer(containerId);
			LoggingService.logInfo(MODULE_NAME, String.format("container \"%s\" removed", containerId));
		} catch (Exception e) {
			LoggingService.logWarning(MODULE_NAME, String.format("error removing container \"%s\"", containerId));
			throw e;
		}
	}

	private void updateContainer() throws Exception {
		stopContainer();
		removeContainer();
		addElement();
		startElement();
	}

	public boolean execute(ContainerTask task) {
		docker = DockerUtil.getInstance();
		if (!docker.isConnected()) {
			try {
				docker.connect();
			} catch (Exception e) {
				return false;
			}
		}
		this.task = task;
		switch (task.action) {
			case ADD:
				try {
					addElement();
					startElement();
					return true;
				} catch (Exception e) {
					return false;
				} finally {
					docker.close();
				}
	
			case REMOVE:
				containerId = task.data.toString();
				try {
					stopContainer();
					removeContainer();
					return true;
				} catch (Exception e) {
					return false;
				} finally {
					docker.close();
				}
	
			case UPDATE:
				containerId = ((Element) task.data).getContainerId();
				try {
					updateContainer();
					return true;
				} catch (Exception e) {
					return false;
				} finally {
					docker.close();
				}
		}
		return true;
	}
}
