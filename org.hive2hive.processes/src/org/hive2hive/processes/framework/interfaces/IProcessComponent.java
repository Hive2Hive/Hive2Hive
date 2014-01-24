package org.hive2hive.processes.framework.interfaces;

import org.hive2hive.processes.framework.ProcessState;

public interface IProcessComponent extends IControllable {

	String getID();

	double getProgress();

	ProcessState getState();

	void join();

	void attachListener(IProcessComponentListener listener);

	void detachListener(IProcessComponentListener listener);
}
