package org.hive2hive.core.test.processes.util;

import org.hive2hive.core.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;

public class SucceedingProcessStep extends ProcessStep {

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		
		// just succeeding!
	}

}
