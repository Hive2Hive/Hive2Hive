package org.hive2hive.core.process;

/**
 * A process has exactly one state at each moment in time.
 * 
 * @author Nico
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
	 * Denotes a stopped process.
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
	 * Denotes a rollbacking process.
	 */
	ROLLBACKING;
}
