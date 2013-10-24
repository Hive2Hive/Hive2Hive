package org.hive2hive.core.process.manager;

import java.util.HashMap;
import java.util.Map;

import org.hive2hive.core.process.IProcess;

/**
 * This class monitors all running processes and is able to pause or stop them.
 * 
 * @author Christian, Nico
 * 
 */
public class ProcessManager {
	private static ProcessManager instance;
	private int pidCounter;
	private Map<Integer, IProcess> controlledProcesses;

	private ProcessManager() {
		// is singleton
		controlledProcesses = new HashMap<Integer, IProcess>();
		pidCounter = 0;
	}

	public static ProcessManager getInstance() {
		if (instance == null) {
			instance = new ProcessManager();
		}
		return instance;
	}

	public int getIdForNewProcess() {
		return pidCounter++ % Integer.MAX_VALUE;
	}

	/**
	 * Attach a process to the ProcessManager such that it is aware of it.
	 * 
	 * @param process
	 */
	public void attachProcess(IProcess process) throws IllegalArgumentException {
		if (!isProcessAttached(process.getID())) {
			controlledProcesses.put(process.getID(), process);
		} else {
			throw new IllegalArgumentException("Process is already attached");
		}
	}

	/**
	 * Detach a process from the ProcessManager such that it no longer knows about it.
	 * 
	 * @param process
	 */
	public void detachProcess(IProcess process) {
		controlledProcesses.remove(process.getID());
	}

	public IProcess getProcess(int processID) {
		return controlledProcesses.get(processID);
	}

	private boolean isProcessAttached(int processID) {
		return controlledProcesses.containsKey(processID);
	}
}
