package com.iotracks.iofabric.process_manager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import com.github.dockerjava.api.command.InspectContainerResponse.ContainerState;
import com.github.dockerjava.api.model.Container;
import com.iotracks.iofabric.element.Element;
import com.iotracks.iofabric.element.ElementManager;
import com.iotracks.iofabric.process_manager.ContainerTask.Tasks;
import com.iotracks.iofabric.status_reporter.StatusReporter;
import com.iotracks.iofabric.supervisor.Supervisor;
import com.iotracks.iofabric.utils.Constants;
import com.iotracks.iofabric.utils.Constants.ElementStatus;
import com.iotracks.iofabric.utils.Constants.ModulesStatus;
import com.iotracks.iofabric.utils.logging.LoggingService;

public class ProcessManager {
	
	private final String MODULE_NAME = "Process Manager";
	private final int MONITOR_CONTAINERS_STATUS_FREQ_SECONDS = 10;
	private ElementManager elementManager;
	private Queue<ContainerTask> tasks;
	public static Boolean updated = true;
	private Object containersMonitorLock = new Object();
	private Object checkTasksLock = new Object();
	private DockerUtil docker;
	private ContainerManager containerManager;
	private static ProcessManager instance;

	private ProcessManager() {}
	
	public static ProcessManager getInstance() {
		if (instance == null) {
			synchronized (ProcessManager.class) {
				if (instance == null)
					instance = new ProcessManager();
			}
		}
		return instance;
	}
	
	public void update() {
		if (!docker.isConnected()) { 
			try {
				docker.connect();
			} catch (Exception e) {
				LoggingService.logWarning(MODULE_NAME, "unable to connect to docker daemon");
				return;
			}
		}
		
		List<Element> elements = elementManager.getElements();
		for (Element element : elements) {
			Container container =  docker.getContainer(element.getElementId());
			if (container != null && !element.isRebuild()) {
				long elementLastModified = element.getLastModified();
				long containerCreated = container.getCreated();
				if (elementLastModified > containerCreated)
					addTask(Tasks.UPDATE, element);
			} else {
				addTask(Tasks.ADD, element);
			}
		}
	}
	
//	private <T> boolean compareLists(List<T> first, List<T> second) {
//		if (first == null && second == null)
//			return true;
//		else if (first == null)
//			return second.isEmpty();
//		else if (second == null)
//			return first.isEmpty();
//		else if (first.size() != second.size())
//			return false;
//		for (T item : first)
//			if (!second.contains(item))
//				return false;
//		return true;
//	}
//
	private long getStartedTime(String date) {
		int milli = Integer.parseInt(date.substring(20, 23));
		date = date.substring(0, 10) + " " + date.substring(11, 19);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		try {
			Date local = dateFormat.parse(dateFormat.format(dateFormat.parse(date)));
			return local.getTime() + milli;
		} catch (Exception e) {
			return 0;
		}
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
		
		synchronized (containersMonitorLock) {
			for (Element element : elementManager.getElements())
				if (!docker.hasContainer(element.getElementId()) || element.isRebuild())
					addTask(Tasks.ADD, element);
			StatusReporter.setProcessManagerStatus().setRunningElementsCount(elementManager.getElements().size());

			List<Container> containers = docker.getContainers();
			for (Container container : containers) {
				Element element = elementManager.getElementById(container.getNames()[0].substring(1));
				
				// element does not exist, remove container
				if (element == null) {	
					addTask(Tasks.REMOVE, container.getId());
					continue;
				}

				element.setContainerId(container.getId());
				try {
					ContainerState status = docker.getContainerStatus(container.getId());
					String containerName = container.getNames()[0].substring(1);
					if (status.isRunning()) {
						StatusReporter.setProcessManagerStatus().getElementStatus(element.getElementId()).setStartTime(getStartedTime(status.getStartedAt()));
						
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
	
	private final Runnable checkTasks = () -> {
		synchronized (checkTasksLock) {
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
	};
	
	public void instanceConfigUpdated() {
		if (docker.isConnected())
			docker.close();
	}
	
	public void start() {
		docker = DockerUtil.getInstance();
		try {
			docker.connect();
		} catch (Exception e) {
			LoggingService.logWarning(MODULE_NAME, "unable to connect to docker daemon");
		}

		tasks = new LinkedList<>();
		elementManager = ElementManager.getInstance();
		containerManager = new ContainerManager();
		
		Supervisor.scheduler.scheduleAtFixedRate(containersMonitor, 0, MONITOR_CONTAINERS_STATUS_FREQ_SECONDS, TimeUnit.SECONDS);
		Supervisor.scheduler.scheduleAtFixedRate(checkTasks, 1, 1, TimeUnit.SECONDS);
		
		StatusReporter.setSupervisorStatus().setModuleStatus(Constants.PROCESS_MANAGER, ModulesStatus.RUNNING);
	}
}
