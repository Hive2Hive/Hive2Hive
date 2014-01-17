package org.hive2hive.processes.implementations.register;

import org.hive2hive.processes.framework.abstracts.ProcessStep;
import org.hive2hive.processes.framework.abstracts.RollbackReason;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;

public class AssureUserInexistentStep extends ProcessStep {

	private IConsumeLocations context;

	public AssureUserInexistentStep(IConsumeLocations context) {
		this.context = context;
	}
	
	@Override
	protected void doExecute() throws InvalidProcessStateException {

		if (context.consumeLocations() != null) {
			cancel(new RollbackReason(this, "Locations already exist."));
		}
	}

	@Override
	protected void doPause() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doResumeExecution() throws InvalidProcessStateException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doResumeRollback() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doRollback(RollbackReason reason) {
		// ignore, step does nothing
	}

}
