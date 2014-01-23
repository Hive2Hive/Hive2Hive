package org.hive2hive.processes.framework.interfaces;

import org.hive2hive.processes.framework.ProcessState;

public interface IProcessComponent extends IControllable {

	public String getID();

	public double getProgress();

	public ProcessState getState();

	public void join();

	public void attachListener(IProcessComponentListener listener);

	public void detachListener(IProcessComponentListener listener);
}
