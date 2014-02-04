package org.hive2hive.core.processes.framework.interfaces;

public interface IProcessResultListener<T> {

	void onResultReady(T result);
}
