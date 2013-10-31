package org.hive2hive.core.test.process;

import org.hive2hive.core.process.listener.IProcessListener;

public class TestProcessListener implements IProcessListener {

	private boolean onSuccess = false;
	private boolean onFailure = false;

	public boolean hasSucceeded() {
		return onSuccess;
	}

	public boolean hasFailed() {
		return onFailure;
	}

	@Override
	public void onSuccess() {
		onSuccess = true;
	}

	@Override
	public void onFail(String reason) {
		onFailure = true;
	}

}