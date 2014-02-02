package org.hive2hive.processes.framework.concretes;

import org.hive2hive.processes.framework.RollbackReason;
import org.hive2hive.processes.framework.interfaces.IProcessComponentListener;

public class ProcessComponentListener implements IProcessComponentListener {

	protected boolean hasSucceeded;
	protected boolean hasFailed;
	protected boolean hasFinished;

	@Override
	public void onSucceeded() {
		hasSucceeded = true;
	}

	@Override
	public void onFailed(RollbackReason reason) {
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
