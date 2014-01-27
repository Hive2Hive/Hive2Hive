package org.hive2hive.processes.test.util;

import org.hive2hive.processes.framework.RollbackReason;
import org.hive2hive.processes.framework.abstracts.ProcessStep;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;

public class FailingProcessStep extends ProcessStep {

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		
		cancel(new RollbackReason(this, "Test process step that must fail."));
	}

}
