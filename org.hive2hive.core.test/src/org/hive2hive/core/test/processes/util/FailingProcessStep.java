package org.hive2hive.core.test.processes.util;

import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;

public class FailingProcessStep extends ProcessStep {

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		
		cancel(new RollbackReason(this, "Test process step that must fail."));
	}

}
