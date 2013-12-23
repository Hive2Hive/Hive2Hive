package org.hive2hive.core.process.listener;

public class ProcessListener implements IProcessListener {

	private boolean hasSucceeded = false;
	private boolean hasFailed = false;
	private Exception exception;

	@Override
	public void onSuccess() {
		hasSucceeded = true;
		hasFailed = false;
	}

	@Override
	public void onFail(Exception exception) {
		this.exception = exception;
		hasFailed = true;
		hasSucceeded = false;
	}

	public boolean hasSucceeded() {
		return hasSucceeded;
	}

	public boolean hasFailed() {
		return hasFailed;
	}

	public Exception getError() {
		return exception;
	}

	public boolean hasFinished() {
		return hasSucceeded() || hasFailed();
	}
}
