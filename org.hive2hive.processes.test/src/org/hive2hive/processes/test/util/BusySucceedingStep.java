package org.hive2hive.processes.test.util;

import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;

public class BusySucceedingStep extends SucceedingProcessStep {

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		
		// super busy
		TestUtil.wait(3000);
		
		super.doExecute();
	}
}
