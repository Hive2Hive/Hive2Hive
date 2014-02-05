package org.hive2hive.core.processes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hive2hive.core.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.abstracts.ProcessComponent;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponent;

/**
 * This class monitors all running processes and is able to pause or stop them.
 * 
 * @author Christian, Nico
 * 
 */
@Deprecated
// Nico: Still used?
public class ProcessManager {
	private Map<String, IProcessComponent> attachedProcesses;

	private static class SingletonHolder {
		private static final ProcessManager INSTANCE = new ProcessManager();
	}

	private ProcessManager() {
		attachedProcesses = new ConcurrentHashMap<String, IProcessComponent>();
	}

	public static ProcessManager getInstance() {
		return SingletonHolder.INSTANCE;
	}

	/**
	 * Stop all currently attached processes.
	 * 
	 * @param reason The reason why all processes get stopped.
	 */
	public void stopAll(String reason) {
		for (IProcessComponent process : getAllProcesses()) {
			try {
				process.cancel(new RollbackReason((ProcessComponent) process,
						"Forced shutdown through ProcessManager"));
			} catch (InvalidProcessStateException e) {
				// ignore
			}
		}
	}

	public IProcessComponent getProcess(String processID) {
		return attachedProcesses.get(processID);
	}

	public List<IProcessComponent> getAllProcesses() {
		return new ArrayList<IProcessComponent>(attachedProcesses.values());
	}

	/**
	 * Attach a process to the {@link ProcessManager} such that it is aware of it.
	 * 
	 * @param process
	 */
	public void attachProcess(IProcessComponent process) throws IllegalArgumentException {
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
	public void detachProcess(IProcessComponent process) {
		attachedProcesses.remove(process.getID());
	}

	private boolean isProcessAttached(String processID) {
		return attachedProcesses.containsKey(processID);
	}
}
