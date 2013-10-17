package org.hive2hive.core.flowcontrol.manager;

import java.util.HashMap;
import java.util.Map;

import org.hive2hive.core.flowcontrol.abstracts.ControlledProcess;
import org.hive2hive.core.flowcontrol.interfaces.IProcess;

/**
 * This class monitors all running processes and is able to pause or stop them.
 * @author Christian
 *
 */
public class ProcessController {

	// eager initialization
	private static final ProcessController instance = null;
	
	private Map<Integer, ControlledProcess> controlledProcesses = new HashMap<Integer, ControlledProcess>();
	
	protected ProcessController(){};
	
	/** 
	 * Attach a process to the ProcessController such that it is aware of it.
	 * @param process
	 */
	public void attachProcess(ControlledProcess process){
		
		if (!isProcessAttached(process.getID())){
			controlledProcesses.put(process.getID(), process);
		}
	}
	
	/**
	 * Detach a process from the ProcessController such that it no longer knows about it.
	 * @param process
	 */
	public void detachProcess(ControlledProcess process){
		
		if (isProcessAttached(process.getID())){
			controlledProcesses.remove(process.getID());
		}
	}
	
	public IProcess getProcess(int processID){
		
		if (isProcessAttached(processID)){
			return controlledProcesses.get(processID);
		}
		return null;
	}
	
	private boolean isProcessAttached(int processID){
		return controlledProcesses.containsKey(processID);
	}
	
	public static ProcessController getInstance() {
		return instance;
	}
}
