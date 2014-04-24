package org.hive2hive.core.processes.framework.interfaces;

import org.hive2hive.core.processes.framework.RollbackReason;

/**
 * Basic process component listener interface.
 * 
 * @author Christian
 * 
 */
public interface IProcessComponentListener {

	/**
	 * Executed if the observed process component succeeded.
	 */
	void onSucceeded();

	/**
	 * Executed if the observed process component failed.
	 */
	void onFailed(RollbackReason reason);
	
}