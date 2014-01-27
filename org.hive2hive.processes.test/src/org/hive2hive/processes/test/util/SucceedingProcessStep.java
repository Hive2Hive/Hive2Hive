package org.hive2hive.processes.test.util;

import org.hive2hive.processes.framework.abstracts.ProcessStep;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;

public class SucceedingProcessStep extends ProcessStep {

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		
		// just succeeding!
	}

}
