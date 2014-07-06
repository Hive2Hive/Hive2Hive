package org.hive2hive.processframework;

import org.hive2hive.processframework.abstracts.ProcessComponent;

/**
 * State of a {@link ProcessComponent}.
 * 
 * @author Christian
 * 
 */
public enum ProcessState {

	/**
	 * Represents a {@link ProcessComponent} that is ready to be executed.
	 */
	READY,
	/**
	 * Represents a {@link ProcessComponent} that is currently being executed.
	 */
	RUNNING,
	/**
	 * Represents a {@link ProcessComponent} that is currently being rolled back.
	 */
	ROLLBACKING,
	/**
	 * Represents a {@link ProcessComponent} that is currently paused, whether it is running or rollbacking.
	 */
	PAUSED,
	/**
	 * Represents a {@link ProcessComponent} that has finished successfully.
	 */
	SUCCEEDED,
	/**
	 * Represents a {@link ProcessComponent} that has finished unsuccessfully and failed.
	 */
	FAILED
}
