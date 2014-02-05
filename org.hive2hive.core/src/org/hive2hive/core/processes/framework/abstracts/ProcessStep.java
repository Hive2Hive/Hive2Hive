package org.hive2hive.core.processes.framework.abstracts;

import org.hive2hive.core.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.RollbackReason;

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
