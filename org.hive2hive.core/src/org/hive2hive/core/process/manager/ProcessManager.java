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
	private Map<Integer, IProcess> attachedProcesses;

	private ProcessManager() {
		attachedProcesses = new HashMap<Integer, IProcess>();
		pidCounter = 0;
	}

	public static ProcessManager getInstance() {
		if (instance == null) {
			instance = new ProcessManager();
		}
		return instance;
	}

	/**
	 * Returns a unique PID (process ID) for this {@link ProcessManager}.
	 * @return A unique PID.
	 */
	public int getNewPID() {
		return pidCounter++ % Integer.MAX_VALUE;
	}

	public IProcess getProcess(int processID) {
		return attachedProcesses.get(processID);
	}

	/**
	 * Attach a process to the {@link ProcessManager} such that it is aware of it.
	 * 
	 * @param process
	 */
	public void attachProcess(IProcess process) throws IllegalArgumentException {
		if (!isProcessAttached(process.getID())) {
			attachedProcesses.put(process.getID(), process);
		} else {
			throw new IllegalArgumentException("Process is already attached");
		}
	}

	/**
	 * Detach a process from the {@link ProcessManager} such that it no longer knows about it.
	 * 
	 * @param process
	 */
	public void detachProcess(IProcess process) {
		attachedProcesses.remove(process.getID());
	}

	private boolean isProcessAttached(int processID) {
		return attachedProcesses.containsKey(processID);
	}
}
