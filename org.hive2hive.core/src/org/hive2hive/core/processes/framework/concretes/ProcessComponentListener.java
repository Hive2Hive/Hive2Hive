package org.hive2hive.core.processes.framework.concretes;

import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponentListener;

/**
 * A basic process component listener that is notified in case of a components success or fail.
 * 
 * @author Christian
 * 
 */
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
