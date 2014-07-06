package org.hive2hive.processframework.util;

import org.hive2hive.processframework.abstracts.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;

public class FailingProcessStep extends ProcessStep {

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		
		throw new ProcessExecutionException("Test process step that must fail.");
	}

}
