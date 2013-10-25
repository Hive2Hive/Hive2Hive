package org.hive2hive.core.test.flowcontrol;

import org.hive2hive.core.process.listener.IProcessListener;

public class TestProcessListener implements IProcessListener {

	public boolean onSuccess = false;
	public boolean onFailure = false;

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