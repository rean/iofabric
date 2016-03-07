package com.iotracks.iofabric.process_manager;

import java.lang.Thread.State;
import java.util.HashMap;
import java.util.Map;

import com.iotracks.iofabric.process_manager.ContainerTask.Tasks;

public class ContainerTaskManager {
	private static Map<ContainerTask, Boolean> todoTasks = new HashMap<>();
	private static Map<ContainerTask, Thread> execTasks = new HashMap<>();
	
	public static void newTask(ContainerTask task) {
		synchronized (todoTasks) {
			for (ContainerTask t : todoTasks.keySet()) {
				if (t.equals(task) || (task.action == Tasks.ADD && t.action == Tasks.UPDATE && t.data.equals(task.data)))
					return;
			}
			todoTasks.put(task, false);
		}
	}
	
	public static ContainerTask getTask() {
		ContainerTask task = null;
		synchronized (todoTasks) {
			for (ContainerTask t : todoTasks.keySet()) {
				if (!todoTasks.get(t)) {			// task has not been executed yet
					task = t;
					todoTasks.put(t, true);			// to be added to execTasks
					break;
				}
			}
		}
		if (task != null) {		// there is a task available
			synchronized (execTasks) {
				execTasks.put(task, null);	// task will be executed
			}
		} else {				// looking for any interrupted tasks to restart 
			synchronized (execTasks) {
				for (ContainerTask t : execTasks.keySet())
					if (execTasks.get(t) != null  && execTasks.get(t).getState() == State.TERMINATED) {
						task = t;
						break;
					}
			}
		}
		return task;
	}
	
	public static void updateTask(ContainerTask task, Thread taskThread) {
		synchronized (execTasks) {
			execTasks.put(task, taskThread);	// task is running
		}
	}
	
	public static void removeTask(ContainerTask task) {	// task id done!
		synchronized (todoTasks) {
			try {
				todoTasks.remove(task);			
			} catch (Exception e) {}
		}
		synchronized (execTasks) {
			try {
				execTasks.remove(task);			
			} catch (Exception e) {}
		}
	}
}