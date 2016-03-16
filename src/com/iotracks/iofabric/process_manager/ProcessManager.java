package com.iotracks.iofabric.process_manager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import com.github.dockerjava.api.command.InspectContainerResponse.ContainerState;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Container.Port;
import com.iotracks.iofabric.element.Element;
import com.iotracks.iofabric.element.ElementManager;
import com.iotracks.iofabric.element.PortMapping;
import com.iotracks.iofabric.process_manager.ContainerTask.Tasks;
import com.iotracks.iofabric.status_reporter.StatusReporter;
import com.iotracks.iofabric.supervisor.Supervisor;
import com.iotracks.iofabric.utils.Constants;
import com.iotracks.iofabric.utils.Constants.ElementStatus;
import com.iotracks.iofabric.utils.Constants.ModulesStatus;
import com.iotracks.iofabric.utils.logging.LoggingService;

public class ProcessManager implements Runnable {
	
	private final String MODULE_NAME = "Process Manager";
	private final int MONITOR_CONTAINERS_STATUS_FREQ_SECONDS = 10;
	private ElementManager elementManager;
	private Queue<ContainerTask> tasks;
	public static Boolean updated = true;
	private Object lock = new Object();
	private DockerUtil docker;

	private Container getContainer(List<Container> containers, String containerName) {
		for (Container container : containers)
			if (container.getNames()[0].substring(1).equals(containerName))
				return container;
		
		return null;
	}
	
	private synchronized boolean updateElements() {
		if (!docker.isConnected()) { 
			try {
				docker.connect();
			} catch (Exception e) {
				LoggingService.logWarning(MODULE_NAME, "unable to connect to docker daemon");
				return false;
			}
		}
		
		synchronized (lock) {
			List<Container> containers = docker.getContainers();
			List<Element> elements = elementManager.getElements();
			for (Element element : elements) {
				Container container = getContainer(containers, element.getElementId());
				if (container != null) {
					containers.remove(container);
					element.setContainerId(container.getId());
					
					try {
						ContainerState status = docker.getContainerStatus(container.getId());
						element.setContainerIpAddress(docker.getContainerIpAddress(container.getId()));
						
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
							StatusReporter.setProcessManagerStatus().getElementStatus(element.getElementId()).setStartTime(started);
						}
					} catch (Exception e) {}
					
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
						addTask(Tasks.UPDATE, element);
					}
				} else {
					addTask(Tasks.ADD, element);
				}
			}
		}
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
	
	private final Runnable containersMonitor = () -> {
		LoggingService.logInfo(MODULE_NAME, "monitoring containers");

		if (!docker.isConnected()) { 
			try {
				docker.connect();
			} catch (Exception e) {
				LoggingService.logWarning(MODULE_NAME, "unable to connect to docker daemon");
				return;
			}
		}
		
		synchronized (lock) {
			List<Container> containers = docker.getContainers();
			for (Element element : elementManager.getElements())
				if (getContainer(containers, element.getElementId()) == null)
					addTask(Tasks.ADD, element);
			
			for (Container container : containers) {
				String elementId = container.getNames()[0].substring(1);
				if (!elementManager.elementExists(elementId)) {
					addTask(Tasks.REMOVE, container.getId());
					continue;
				}
				
				try {
					ContainerState status = docker.getContainerStatus(container.getId());
					String containerName = container.getNames()[0].substring(1);
		
					if (status.isRunning()) {
						StatusReporter.setProcessManagerStatus().getElementStatus(containerName).setStatus(ElementStatus.RUNNING);
						LoggingService.logInfo(MODULE_NAME,
								String.format("\"%s\": container is running", containerName));
					} else {
						StatusReporter.setProcessManagerStatus().getElementStatus(containerName).setStatus(ElementStatus.STOPPED);
						LoggingService.logInfo(MODULE_NAME,
								String.format("\"%s\": container stopped", containerName));
						try {
							LoggingService.logInfo(MODULE_NAME,
									String.format("\"%s\": starting", containerName));
							docker.startContainer(container.getId());
							StatusReporter.setProcessManagerStatus().getElementStatus(containerName).setStatus(ElementStatus.RUNNING);
							LoggingService.logInfo(MODULE_NAME,
									String.format("\"%s\": started", containerName));
						} catch (Exception startException) {
							// unable to start the container, update it!
							addTask(Tasks.UPDATE, container.getId());
						}
					}
				} catch (Exception e) {}
			}
		}
	};
	
	private void addTask(Tasks action, Object data) {
		ContainerTask task = new ContainerTask(action, data);
		synchronized (tasks) {
			if (!tasks.contains(task))
				tasks.add(task);
		}
	}
	
	private Runnable checkUpdated = () -> {
		if (updated)
			updated = !updateElements();
	};
	
	@Override
	public void run() {
		docker = DockerUtil.getInstance();
		try {
			docker.connect();
		} catch (Exception e) {
			LoggingService.logWarning(MODULE_NAME, "unable to connect to docker daemon");
		}

		tasks = new LinkedList<>();
		elementManager = ElementManager.getInstance();
		
		Supervisor.scheduler.scheduleAtFixedRate(containersMonitor, 0, MONITOR_CONTAINERS_STATUS_FREQ_SECONDS, TimeUnit.SECONDS);
		Supervisor.scheduler.scheduleAtFixedRate(checkUpdated, 0, 1, TimeUnit.SECONDS);
		
		StatusReporter.setSupervisorStatus().setModuleStatus(Constants.PROCESS_MANAGER, ModulesStatus.RUNNING);
		
		ContainerManager containerManager = new ContainerManager();
		while (true) {
			ContainerTask newTask = null;
			synchronized (tasks) {
				newTask = tasks.poll();
			}
			if (newTask != null) {
				boolean taskResult = containerManager.execute(newTask);
				if (!taskResult)
					addTask(newTask.action, newTask.data);
			}
		}
		
	}
}
