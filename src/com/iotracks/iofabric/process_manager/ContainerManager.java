package com.iotracks.iofabric.process_manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.RestartPolicy;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.iotracks.iofabric.element.Element;
import com.iotracks.iofabric.process_manager.ContainerTask.Tasks;
import com.iotracks.iofabric.status_reporter.StatusReporter;
import com.iotracks.iofabric.utils.Constants.ElementStatus;
import com.iotracks.iofabric.utils.configuration.Configuration;
import com.iotracks.iofabric.utils.logging.LoggingService;

public class ContainerManager implements Runnable {

	private ContainerTask task;
	private String containerId;
	private DockerClient dockerClient;

	private static Object imagePullLock = new Object();
	
	private final String MODULE_NAME = "Container Manager";
	private final String IOFABRIC_HOST = "iofabric:127.0.0.1";

	public ContainerManager(ContainerTask task) {
		this.task = task;
	}
	
	public void run() {
		try {
			dockerLogin();
		} catch (Exception e) {
			// login failed. cannot continue!
			return;
		}
		
		if (task.action.equals(Tasks.ADD)) {
			try { // TODO: check for remaining remove commands. may cause conflicts!
				addElement();
				ContainerTaskManager.removeTask(task);
				startElement();
			} catch (Exception e){}
		} else if (task.action.equals(Tasks.REMOVE)) {
			containerId = task.data.toString();
			try {
				stopContainer();
				removeContainer();
				ContainerTaskManager.removeTask(task);
			} catch (Exception e) {
			}
		} else if (task.action.equals(Tasks.UPDATE)) {  // TODO: check for remaining remove commands. may cause conflicts!
			containerId = ((Element) task.data).getContainerId();
			updateContainer();
		}

		try {
			dockerClient.close();
		} catch (Exception e) {
		}
	}

	private boolean containerExists() {
		try {
			dockerClient.inspectContainerCmd(containerId).exec();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private void dockerLogin() throws Exception {
		LoggingService.logInfo(MODULE_NAME, "logging in to registry");
		try {
			DockerClientConfig config = DockerClientConfig.createDefaultConfigBuilder()
					.withUri("unix:///var/run/docker.sock")
					.withDockerCertPath(Configuration.getControllerCert())
					.withUsername("iointegrator")
					.withPassword("0nTh3Edge2015")
					.withEmail("admin@iotracks.com")
					.withServerAddress("https://index.docker.io/v1/")
					.build();
			dockerClient = DockerClientBuilder.getInstance(config).build();

			dockerClient.authCmd().exec();
		} catch (Exception e) {
			LoggingService.logWarning(MODULE_NAME, "login failed - " + e.getMessage());
			throw e;
		}
	}

	private void addElement() {
		// TODO : pull image and create container
		Element element = (Element) task.data;
		StatusReporter.setProcessManagerStatus().setElementsStatus(element.getElementId(), ElementStatus.BUILDING);
		LoggingService.logInfo(MODULE_NAME, "building \"" + element.getImageName() + "\"");
		
		List<Container> containers = dockerClient.listContainersCmd().withShowAll(true).exec();
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
		PullImageCmd req = dockerClient.pullImageCmd(registry).withAuthConfig(dockerClient.authConfig());
		if (tag != null)
			req.withTag(tag);

		try {
			synchronized (imagePullLock) {
				LoggingService.logInfo(MODULE_NAME, "pulling \"" + element.getImageName() + "\" from registry");
				PullImageResultCallback res = new PullImageResultCallback();
				res = req.exec(res);
				res.awaitSuccess();
				LoggingService.logInfo(MODULE_NAME, String.format("\"%s\" pulled", element.getImageName()));
			}

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

			CreateContainerResponse resp = dockerClient.createContainerCmd(element.getImageName())
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
			StatusReporter.setProcessManagerStatus().setElementsStatus(element.getElementId(), ElementStatus.FAILED_VERIFICATION);
			throw ex;
		}
	}

	private void startElement() {
		Element element = (Element) task.data;
		StatusReporter.setProcessManagerStatus().setElementsStatus(element.getElementId(), ElementStatus.STARTING);
		LoggingService.logInfo(MODULE_NAME, String.format("starting container \"%s\"", element.getImageName()));
		// TODO
		try {
			dockerClient.startContainerCmd(element.getContainerId()).exec();
			LoggingService.logInfo(MODULE_NAME, String.format("\"%s\" started", element.getImageName()));
			StatusReporter.setProcessManagerStatus().setElementsStatus(element.getElementId(), ElementStatus.RUNNING);
		} catch (Exception ex) {
			LoggingService.logWarning(MODULE_NAME,
					String.format("container \"%s\" not found - %s", element.getImageName(), ex.getMessage()));
			StatusReporter.setProcessManagerStatus().setElementsStatus(element.getElementId(), ElementStatus.STOPPED);
		}
	}
	
	private void stopContainer() {
		// TODO: stop container
		LoggingService.logInfo(MODULE_NAME, String.format("stopping container \"%s\"", containerId));
		try {
			dockerClient.stopContainerCmd(containerId).exec();
			LoggingService.logInfo(MODULE_NAME, String.format("container \"%s\" stopped", containerId));
		} catch (Exception e) {
			LoggingService.logWarning(MODULE_NAME, String.format("error stopping container \"%s\"", containerId));
		}
	}

	private void removeContainer() {
		// TODO: remove container
		if (!containerExists())
			return;
		LoggingService.logInfo(MODULE_NAME, String.format("removing container \"%s\"", containerId));
		try {
			dockerClient.removeContainerCmd(containerId).withForce(true).exec();
			LoggingService.logInfo(MODULE_NAME, String.format("container \"%s\" removed", containerId));
		} catch (Exception e) {
			LoggingService.logWarning(MODULE_NAME, String.format("error removing container \"%s\"", containerId));
			throw e;
		}
	}

	private void updateContainer() {
		// TODO: update container
		try {
			stopContainer();
			removeContainer();
			addElement();
			ContainerTaskManager.removeTask(task);
			startElement();
		} catch (Exception e) {
			
		}
	}
}
