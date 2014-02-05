package org.hive2hive.core.test.processes.util;

import org.hive2hive.core.exceptions.InvalidProcessStateException;

public class BusySucceedingStep extends SucceedingProcessStep {

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		
		// super busy
		TestUtil.wait(3000);
		
		super.doExecute();
	}
}
