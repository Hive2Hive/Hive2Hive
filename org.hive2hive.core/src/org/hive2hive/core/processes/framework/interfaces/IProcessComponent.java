package org.hive2hive.core.processes.framework.interfaces;

import org.hive2hive.core.processes.framework.ProcessState;

public interface IProcessComponent extends IControllable {

	void attachListener(IProcessComponentListener listener);

	void detachListener(IProcessComponentListener listener);

	String getID();

	double getProgress();

	ProcessState getState();
	
}
