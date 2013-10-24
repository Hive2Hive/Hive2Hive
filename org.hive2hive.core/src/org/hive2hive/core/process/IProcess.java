package org.hive2hive.core.process;

import org.hive2hive.core.process.listener.IProcessListener;

/**
 * This interface provides the default methods of a process which represents a use case.
 * 
 * @author Christian, Nico
 * 
 */
public interface IProcess extends Runnable {

	void pause();

	void continueProcess();

	void stop();

	void start();

	int getProgress();

	int getID();

	ProcessState getState();
	
	void addListener(IProcessListener listener);
	
	boolean removeListener(IProcessListener listener);

}
