package org.hive2hive.core.test.processes.util;

import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;

public class SucceedingProcessStep extends ProcessStep {

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		
		// just succeeding!
	}

}
