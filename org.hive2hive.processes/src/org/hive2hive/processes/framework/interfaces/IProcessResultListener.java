package org.hive2hive.processes.framework.interfaces;

public interface IProcessResultListener<T> extends IProcessComponentListener {

	public void onResultReady(T result);
}
