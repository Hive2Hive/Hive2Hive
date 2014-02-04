package org.hive2hive.core.processes.framework.interfaces;

public interface IResultProcessComponent<T> extends IProcessComponent {

	void attachListener(IProcessResultListener<T> listener);
	
	void detachListener(IProcessResultListener<T> listener);

}