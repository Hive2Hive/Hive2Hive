package org.hive2hive.processes.framework.interfaces;

import org.hive2hive.processes.framework.ProcessState;

public interface IProcessComponent extends IControllable, IRollbackable {

	public String getID();
	
	public double getProgress();
	
	public ProcessState getState();
	
	public void join();
}
