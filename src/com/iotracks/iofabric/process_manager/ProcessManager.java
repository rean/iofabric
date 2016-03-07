package com.iotracks.iofabric.process_manager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse.ContainerState;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Container.Port;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.iotracks.iofabric.element.Element;
import com.iotracks.iofabric.element.ElementManager;
import com.iotracks.iofabric.element.PortMapping;
import com.iotracks.iofabric.process_manager.ContainerTask.Tasks;
import com.iotracks.iofabric.status_reporter.StatusReporter;
import com.iotracks.iofabric.utils.Constants;
import com.iotracks.iofabric.utils.Constants.ElementStatus;
import com.iotracks.iofabric.utils.Constants.ModulesStatus;
import com.iotracks.iofabric.utils.configuration.Configuration;
import com.iotracks.iofabric.utils.logging.LoggingService;

public class ProcessManager implements Runnable {
	
	private final String MODULE_NAME = "Process Manager";
	private final int MONITOR_CONTAINERS_STATUS_FREQ_SECONDS = 10;
	private DockerClient dockerClient;
	private final ElementManager elementManager = new ElementManager();

	private boolean init() {
		if (!dockerConnected()) { 
			try {
				dockerConnect();
			} catch (Exception e) {
				LoggingService.logWarning(MODULE_NAME, "unable to connect to docker daemon");
				return false;
			}
		}
		
		List<Container> containers = dockerClient.listContainersCmd().withShowAll(true).exec();
		List<Element> elements = new ArrayList<>();
		elements = elementManager.getElements();
		synchronized (ElementManager.class) {
			if (elements != null) {
				for (Element element : elements) {
					Container container = null;
					Optional<Container> result = containers.stream()
							.filter(c -> c.getNames()[0].trim().substring(1).equals(element.getElementId())).findFirst();
					if (result.isPresent())
						container = result.get();

					if (container != null) {
						containers.remove(container);
						element.setContainerId(container.getId());
						
						InspectContainerResponse inspect = dockerClient.inspectContainerCmd(container.getId()).exec();
						element.setContainerIpAddress(inspect.getNetworkSettings().getIpAddress());
						
						ContainerState status = inspect.getState();
						if (status.isRunning()) {
							String date = status.getStartedAt();
							int milli = Integer.parseInt(date.substring(20, 23));
							date = date.substring(0, 10) + " " + date.substring(11, 19);
							DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
							long started = 0;
							try {
								Date local = dateFormat.parse(dateFormat.format(dateFormat.parse(date)));
								started = local.getTime() + milli;
							} catch (Exception e) {
							}
							element.setStartTime(started);
						}
						
						long elementLastModified = element.getLastModified();
						long containerCreated = container.getCreated();
						
						List<PortMapping> containerPortMappings = new ArrayList<>();
						Port[] containerPorts = container.getPorts(); 
						if (containerPorts != null)
							for (Port port : containerPorts) {
								containerPortMappings.add(new PortMapping(port.getPublicPort().toString(), port.getPrivatePort().toString()));
							}
						
						List<PortMapping> elementPortMappings = element.getPortMappings();
						
						if (!compareLists(elementPortMappings, containerPortMappings) || elementLastModified > containerCreated) {
							update(Tasks.UPDATE, element);
						}
					} else {
						update(Tasks.ADD, element);
					}
				}
			}
		}

		containers.forEach(c -> {
			update(Tasks.REMOVE, c.getId());
		});

		return true;
	}
	
	private <T> boolean compareLists(List<T> first, List<T> second) {
		if (first == null && second == null)
			return true;
		else if (first == null)
			return second.isEmpty();
		else if (second == null)
			return first.isEmpty();
		else if (first.size() != second.size())
			return false;
		for (T item : first)
			if (!second.contains(item))
				return false;
		return true;
	}
	
	private boolean dockerConnected() {
		try {
			dockerClient.infoCmd().exec();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	private final Runnable containersMonitor = () -> {
		LoggingService.logInfo(MODULE_NAME, "monitoring containers");

		if (!dockerConnected()) { 
			try {
				dockerConnect();
			} catch (Exception e) {
				LoggingService.logWarning(MODULE_NAME, "unable to connect to docker daemon");
				return;
			}
		}
		
		List<Container> containers = dockerClient.listContainersCmd().withShowAll(true).exec();

		containers.forEach(container -> {
			InspectContainerResponse containerInfo = dockerClient.inspectContainerCmd(container.getId())
					.exec();
			ContainerState status = containerInfo.getState();
			String containerName = container.getNames()[0].substring(1);

			if (status.isRunning()) {
				StatusReporter.setProcessManagerStatus().setElementsStatus(containerName, ElementStatus.RUNNING);
				LoggingService.logInfo(MODULE_NAME,
						String.format("\"%s\": container is running", containerName));
			} else {
				StatusReporter.setProcessManagerStatus().setElementsStatus(containerName,
						ElementStatus.STOPPED);
				LoggingService.logInfo(MODULE_NAME,
						String.format("\"%s\": container stopped", containerName));
				try {
					LoggingService.logInfo(MODULE_NAME,
							String.format("\"%s\": starting", containerName));
					dockerClient.startContainerCmd(container.getId()).exec();
					StatusReporter.setProcessManagerStatus().setElementsStatus(containerName,
							ElementStatus.RUNNING);
					LoggingService.logInfo(MODULE_NAME,
							String.format("\"%s\": started", containerName));
				} catch (Exception startException) {
					// unable to start the container, update it!
					update(Tasks.UPDATE, container.getId());
				}
			}
		});
	};
	
	public static void update(Tasks action, Object data) {
		ContainerTaskManager.newTask(new ContainerTask(action, data));
	}
	
	private void dockerConnect() throws Exception {
		DockerClientConfig config = DockerClientConfig.createDefaultConfigBuilder()
				.withUri(Configuration.getDockerUrl())
				.withDockerCertPath(Configuration.getControllerCert())
				.build();
		dockerClient = DockerClientBuilder.getInstance(config).build();

		try {
			Info info = dockerClient.infoCmd().exec();
			LoggingService.logInfo(MODULE_NAME, "connected to docker daemon: " + info.getName());
		} catch (Exception e) {
			throw e;
		}
	}
	
	@Override
	public void run() {
		try {
			dockerConnect();
		} catch (Exception e) {
			LoggingService.logWarning(MODULE_NAME, "unable to connect to docker daemon");
		}
		
		LoggingService.logInfo(MODULE_NAME, "initializing...");
		while (!init()) {
			try {
				Thread.sleep(100);
			} catch (Exception e) {
				
			}
		}
		LoggingService.logInfo(MODULE_NAME, "initialization done");
		
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(containersMonitor, 0, MONITOR_CONTAINERS_STATUS_FREQ_SECONDS,	TimeUnit.SECONDS);
		
		StatusReporter.setSupervisorStatus().setModuleStatus(Constants.PROCESS_MANAGER, ModulesStatus.RUNNING);
		
		int delay;
		while (true) {
			ContainerTask newTask = ContainerTaskManager.getTask();
			if (newTask != null) {
				delay = 50;
				Thread taskThread = new Thread(new ContainerManager(newTask), "ContainerManager (" + newTask.action + ")");
				ContainerTaskManager.updateTask(newTask, taskThread);
				taskThread.start();
			} else {
				delay = 1000;
				init();
			}
			try {
				Thread.sleep(delay);
			} catch (Exception e) {
				
			}
		}
		
	}
}
