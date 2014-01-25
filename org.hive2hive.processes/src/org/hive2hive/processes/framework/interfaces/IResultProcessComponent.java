package org.hive2hive.processes.framework.interfaces;

public interface IResultProcessComponent<T> {

	void attachListener(IProcessResultListener<T> listener);
	
	void detachListener(IProcessResultListener<T> listener);

	public void notifyResultComputed(T result);
	
}