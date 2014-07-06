package org.hive2hive.processframework.concretes;

import org.hive2hive.processframework.RollbackReason;
import org.hive2hive.processframework.interfaces.IProcessComponentListener;

/**
 * A basic process component listener that is notified in case of a components success or fail.
 * 
 * @author Christian
 * 
 */
public class ProcessComponentListener implements IProcessComponentListener {

	protected boolean hasSucceeded = false;
	protected boolean hasFailed = false;
	protected boolean hasFinished = false;
	
	protected RollbackReason rollbackReason;

	@Override
	public void onSucceeded() {
		hasSucceeded = true;
		hasFinished = true;
		rollbackReason = null;
	}

	@Override
	public void onFailed(RollbackReason reason) {
		hasFailed = true;
		hasFinished = true;
		rollbackReason = reason;
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
	
	/**
	 * Returns the {@link RollbackReason} in case of a failure and <code>null</code> otherwise.
	 * @return The {@link RollbackReason} in case of a failure, <code>null</code> otherwise.
	 */
	public RollbackReason getRollbackReason() {
		return rollbackReason;
	}

}
