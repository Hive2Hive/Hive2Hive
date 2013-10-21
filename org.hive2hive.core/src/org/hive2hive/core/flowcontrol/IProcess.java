package org.hive2hive.core.flowcontrol;

/**
 * This interface provides the default methods of a process which represents a use case.
 * 
 * @author Christian, Nico
 * 
 */
public interface IProcess {

	void pause();

	void continueProcess();

	void stop();

	void start();

	int getProgress();

	int getID();

	ProcessState getState();

}
