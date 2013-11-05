package org.hive2hive.core.process;

import org.hive2hive.core.process.listener.IProcessListener;
import org.hive2hive.core.process.manager.ProcessManager;

/**
 * This interface provides the default methods of a process which represents a use case.
 * 
 * @author Christian, Nico
 * 
 */
public interface IProcess extends Runnable {

	/**
	 * Starts the process, such that its state will be {@link ProcessState#RUNNING}. Only processes being in
	 * {@link ProcessState#INITIALIZING} state can be started.
	 */
	void start();

	/**
	 * Pause the process, such that its state will be {@link ProcessState#PAUSED}. Only processes being in
	 * {@link ProcessState#RUNNING} state can be paused. The currently running {@link ProcessStep} will
	 * finish. The process will pause before the next {@link ProcessStep}.
	 */
	void pause();

	/**
	 * Continues the process, such that its state will be {@link ProcessState#RUNNING}. Only processes being in
	 * {@link ProcessState#PAUSED} state can be continued.
	 */
	void continueProcess();

	/**
	 * Stops the process, such that its state will be {@link ProcessState#STOPPED}.
	 */
	void stop();

	/**
	 * Returns the per-{@link ProcessManager} unique PID of this process.
	 * @return This process' PID.
	 */
	int getID();

	/**
	 * Returns the current {@link ProcessState} of this process.
	 * @return This process' current state.
	 */
	ProcessState getState();

	/**
	 * Returns the current progress of this process.
	 * @return This process' current progress.
	 */
	int getProgress();

	void addListener(IProcessListener listener);

	boolean removeListener(IProcessListener listener);

}
