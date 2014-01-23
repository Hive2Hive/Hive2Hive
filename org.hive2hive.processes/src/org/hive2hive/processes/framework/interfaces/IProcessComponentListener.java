package org.hive2hive.processes.framework.interfaces;

public interface IProcessComponentListener {

	public void onSucceeded();

	public void onFailed();

	public void onFinished();
}
