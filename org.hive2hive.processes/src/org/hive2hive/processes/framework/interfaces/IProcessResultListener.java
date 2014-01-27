package org.hive2hive.processes.framework.interfaces;

public interface IProcessResultListener<T> {

	void onResultReady(T result);
}
