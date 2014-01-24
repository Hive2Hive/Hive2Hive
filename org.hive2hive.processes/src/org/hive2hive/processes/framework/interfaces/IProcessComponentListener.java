package org.hive2hive.processes.framework.interfaces;

public interface IProcessComponentListener {

	void onSucceeded();

	void onFailed();

	void onFinished();
}
