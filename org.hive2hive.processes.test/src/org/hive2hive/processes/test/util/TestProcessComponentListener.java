package org.hive2hive.processes.test.util;

import org.hive2hive.processes.framework.interfaces.IProcessComponentListener;

public class TestProcessComponentListener implements IProcessComponentListener {

	private boolean onSuccess = false;
	private boolean onFailure = false;

	public boolean hasSucceeded() {
		return onSuccess;
	}

	public boolean hasFailed() {
		return onFailure;
	}

	public void reset() {
		onSuccess = false;
		onFailure = false;
	}

	@Override
	public void onSucceeded() {
		onSuccess = true;
	}

	@Override
	public void onFailed() {
		onFailure = true;
	}

	@Override
	public void onFinished() {
		// ignore
	}

}