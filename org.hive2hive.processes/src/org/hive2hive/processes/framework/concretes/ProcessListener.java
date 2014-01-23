package org.hive2hive.processes.framework.concretes;

import org.hive2hive.processes.framework.interfaces.IProcessComponentListener;

public class ProcessListener implements IProcessComponentListener {

	private boolean hasSucceeded;
	private boolean hasFailed;
	private boolean hasFinished;

	@Override
	public void onSucceeded() {
		hasSucceeded = true;
	}

	@Override
	public void onFailed() {
		hasFailed = true;
	}

	@Override
	public void onFinished() {
		hasFinished = true;
	}

	public boolean hasSucceeded() {
		return hasSucceeded;
	}

	public boolean hasFailed() {
		return hasFailed;
	}

	public boolean hasFinished() {
		return hasFinished;
	}

}
