package org.hive2hive.processes.framework.abstracts;

import org.hive2hive.processes.framework.RollbackReason;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;

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
