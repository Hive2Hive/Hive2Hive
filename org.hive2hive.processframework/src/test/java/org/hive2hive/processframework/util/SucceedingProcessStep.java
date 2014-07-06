package org.hive2hive.processframework.util;

import org.hive2hive.processframework.abstracts.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;

public class SucceedingProcessStep extends ProcessStep {

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		
		// just succeeding!
	}

}
