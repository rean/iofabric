package com.iotracks.iofabric.process_manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.RestartPolicy;
import com.github.dockerjava.core.command.PullImageResultCallback;
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

	private final String MODULE_NAME = "Container Manager";
	private final String IOFABRIC_HOST = "iofabric:127.0.0.1";

	public ContainerManager() {
	}
	
	private boolean containerExists() {
		try {
			docker.getContainerStatus(containerId);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	private void addElement() throws Exception {
		Element element = (Element) task.data;

		try {
			Registry registry = ElementManager.getInstance().getRegistry(element.getRegistry());
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
		
		List<Container> containers = docker.getContainers();
		Optional<Container> result = containers.stream()
				.filter(c -> c.getNames()[0].trim().substring(1).equals(element.getElementId())).findFirst();
		if (result.isPresent())
			return;
		
		
		String tag = null;
		String registry = element.getImageName();
		if (registry.contains(":")) {
			String[] sp = registry.split(":");
			registry = sp[0];
			tag = sp[1];
		}
		PullImageCmd req = docker.getDockerClient().pullImageCmd(registry).withAuthConfig(docker.getDockerClient().authConfig());
		if (tag != null)
			req.withTag(tag);

		try {
			LoggingService.logInfo(MODULE_NAME, "pulling \"" + element.getImageName() + "\" from registry");
			PullImageResultCallback res = new PullImageResultCallback();
			res = req.exec(res);
			res.awaitSuccess();
			LoggingService.logInfo(MODULE_NAME, String.format("\"%s\" pulled", element.getImageName()));

			LoggingService.logInfo(MODULE_NAME, "creating container");
			RestartPolicy restartPolicy = RestartPolicy.onFailureRestart(10);

			Ports portBindings = new Ports();
			List<ExposedPort> exposedPorts = new ArrayList<>();
			if (element.getPortMappings() != null)
				element.getPortMappings().forEach(mapping -> {
					ExposedPort internal = ExposedPort.tcp(Integer.parseInt(mapping.getInside()));
					Ports.Binding external = Ports.Binding(Integer.parseInt(mapping.getOutside()));
					portBindings.bind(internal, external);
					exposedPorts.add(internal);
				});
			String[] extraHosts = { IOFABRIC_HOST };

			CreateContainerResponse resp = docker.getDockerClient().createContainerCmd(element.getImageName())
					.withCpuset("0")
					.withExtraHosts(extraHosts)
					.withExposedPorts(exposedPorts.toArray(new ExposedPort[0]))
					.withPortBindings(portBindings)
					.withEnv("SELFNAME=" + element.getElementId())
					.withName(element.getElementId())
					.withRestartPolicy(restartPolicy)
					.exec();
			element.setContainerId(resp.getId());
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
		if (!containerExists())
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
