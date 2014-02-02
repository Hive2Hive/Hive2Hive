package org.hive2hive.processes.framework.interfaces;

import org.hive2hive.processes.framework.ProcessState;
import org.hive2hive.processes.framework.abstracts.Process;

public interface IProcessComponent extends IControllable {

	void attachListener(IProcessComponentListener listener);

	void detachListener(IProcessComponentListener listener);

	String getID();

	double getProgress();

	ProcessState getState();
	
	void setParent(Process parent);
	
	Process getParent();
	
}
