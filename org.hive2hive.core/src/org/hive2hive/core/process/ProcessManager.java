package org.hive2hive.core.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

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
		attachedProcesses = new ConcurrentHashMap<Integer, IProcess>();
		pidCounter = 0;
	}

	public static ProcessManager getInstance() {
		if (instance == null) {
			instance = new ProcessManager();
		}
		return instance;
	}

	/**
	 * Creates a random PID which is not really valid (it's negative). This is used because some code parts
	 * rely on a PID
	 * 
	 * @return
	 */
	public static int createRandomPseudoPID() {
		// create simulated PID
		return new Random().nextInt(Integer.MAX_VALUE) * -1;
	}

	/**
	 * Stop all currently attached processes.
	 * 
	 * @param reason The reason why all processes get stopped.
	 */
	public void stopAll(String reason) {
		for (IProcess process : getAllProcesses()) {
			process.stop(reason);
		}
	}

	/**
	 * Returns a unique PID (process ID) for this {@link ProcessManager}.
	 * 
	 * @return A unique PID.
	 */
	public synchronized int getNewPID() {
		return pidCounter++ % Integer.MAX_VALUE;
	}

	public IProcess getProcess(int processID) {
		return attachedProcesses.get(processID);
	}

	public List<IProcess> getAllProcesses() {
		return new ArrayList<IProcess>(attachedProcesses.values());
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
