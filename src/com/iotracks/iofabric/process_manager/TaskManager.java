package com.iotracks.iofabric.process_manager;

import java.util.LinkedList;
import java.util.Queue;

public class TaskManager {
	private Queue<ContainerTask> tasks;
	
	public TaskManager() {
		tasks = new LinkedList<>();
	}
	
	public void addTask(ContainerTask task) {
		synchronized (this.tasks) {
			if (!tasks.contains(task))
				tasks.add(task);
		}
	}
	
	public ContainerTask getTask() {
		synchronized (this.tasks) {
			return tasks.peek();
		}
	}
	
	public void removeTask(ContainerTask task) {
		synchronized (this.tasks) {
			tasks.remove(task);
		}
	}
}
