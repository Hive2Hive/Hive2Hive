package org.hive2hive.core.process;

/**
 * A process has exactly one state at each moment in time.
 * 
 * @author Nico, Christian
 * 
 */
public enum ProcessState {
	/**
	 * Denotes a running process.
	 */
	RUNNING,
	/**
	 * Denotes a paused process.
	 */
	PAUSED,
	/**
	 * Denotes a stopped process that has been rolled back.
	 */
	STOPPED,
	/**
	 * Denotes a finished process.
	 */
	FINISHED,
	/**
	 * Denotes an initializing process.
	 */
	INITIALIZING,
	/**
	 * Denotes a process that is currently rolled back.
	 */
	ROLLBACKING;
}
