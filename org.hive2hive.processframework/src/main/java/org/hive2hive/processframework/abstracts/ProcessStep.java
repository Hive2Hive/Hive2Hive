package org.hive2hive.processframework.abstracts;

import org.hive2hive.processframework.RollbackReason;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;

/**
 * Abstract base class for all (leaf) process components that represent a specific operation and do not
 * contain further components.
 * 
 * @author Christian
 * 
 */
public abstract class ProcessStep extends ProcessComponent {

	@Override
	protected final void doPause() {
		// TODO Auto-generated method stub

	}

	@Override
	protected final void doResumeExecution() throws InvalidProcessStateException {
		// TODO Auto-generated method stub

	}

	@Override
	protected final void doResumeRollback() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {
		// do nothing by default
	}
}
