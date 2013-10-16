package org.hive2hive.core.flowcontrol.manager;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.hive2hive.core.flowcontrol.interfaces.IProcess;

/**
 * This class represents the overall flow control manager of the Hive2Hive processes. It is implemented as a Singleton with eager initialization
 * as an instance will always be used. ProcessManager is responsible to coordinate all ongoing and incoming processes.
 * @author Christian
 *
 */
public class ProcessManager {

	// eager initialization
	private static final ProcessManager instance = null;
	
	private Queue<IProcess> processQueue = new LinkedList<IProcess>();
	
	private Set<IProcess> runningProcesses = new HashSet<IProcess>();
	
	protected ProcessManager(){};
	
	public static ProcessManager getInstance() {
		return instance;
	}
	
	public void addProcess(IProcess process){
		processQueue.add(process);
	}
	
	public void removeProcess(IProcess process){
		// TODO implement
	}
	
	public void run(){
		
//		while(processQueue.iterator().hasNext()){
//			processQueue.iterator().next().process();
//		}
		
	}
}
