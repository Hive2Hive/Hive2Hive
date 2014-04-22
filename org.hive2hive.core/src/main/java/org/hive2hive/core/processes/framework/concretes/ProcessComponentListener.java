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
	
	protected RollbackReason rollbackReason;

	@Override
	public void onSucceeded() {
		hasSucceeded = true;
		rollbackReason = null;
	}

	@Override
	public void onFailed(RollbackReason reason) {
		hasFailed = true;
		rollbackReason = reason;
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
	
	/**
	 * Returns the {@link RollbackReason} in case of a failure and <code>null</code> otherwise.
	 * @return The {@link RollbackReason} in case of a failure, <code>null</code> otherwise.
	 */
	public RollbackReason getRollbackReason() {
		return rollbackReason;
	}

}
