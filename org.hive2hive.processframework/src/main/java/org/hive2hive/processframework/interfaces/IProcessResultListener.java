package org.hive2hive.processframework.interfaces;

/**
 * Listener interface for process components that deliver a result.
 * 
 * @author Christian
 * 
 * @param <T> The type of the result object.
 */
public interface IProcessResultListener<T> {

	/**
	 * Executed if the observed process component is ready to provide its computed result.
	 */
	void onResultReady(T result);
}
