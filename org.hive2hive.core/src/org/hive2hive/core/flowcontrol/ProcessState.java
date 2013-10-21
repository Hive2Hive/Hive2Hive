package org.hive2hive.core.flowcontrol;

/**
 * A process has exactly one state at each moment in time
 * 
 * @author Nico
 * 
 */
public enum ProcessState {
	RUNNING,
	PAUSED,
	STOPPED,
	FINISHED,
	INITIALIZING;
}
