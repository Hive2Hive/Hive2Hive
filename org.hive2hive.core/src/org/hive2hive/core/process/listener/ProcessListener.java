package org.hive2hive.core.process.listener;


public class ProcessListener implements IProcessListener {

	private boolean hasSucceeded = false;
	private boolean hasFailed = false;
	
	@Override
	public void onSuccess() {
		hasSucceeded = true;
		hasFailed = false;
	}

	@Override
	public void onFail(String reason) {
		hasFailed = true;
		hasSucceeded = false;
	}

	public boolean hasSucceeded() {
		return hasSucceeded;
	}


	public boolean hasFailed() {
		return hasFailed;
	}
	
	public boolean hasFinished() {
		return hasSucceeded() || hasFailed();
	}
}
