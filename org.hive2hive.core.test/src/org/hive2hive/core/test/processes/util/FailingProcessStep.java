package org.hive2hive.core.test.processes.util;

import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;

public class FailingProcessStep extends ProcessStep {

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		
		throw new ProcessExecutionException("Test process step that must fail.");
	}

}
