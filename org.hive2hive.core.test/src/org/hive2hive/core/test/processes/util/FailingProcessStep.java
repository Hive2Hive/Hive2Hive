package org.hive2hive.core.test.processes.util;

import org.hive2hive.core.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;

public class FailingProcessStep extends ProcessStep {

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		
		cancel(new RollbackReason(this, "Test process step that must fail."));
	}

}
