package org.hive2hive.core.processes.util;

import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;

public class BusySucceedingStep extends SucceedingProcessStep {

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		
		// super busy
		TestUtil.waitDefault();
		
		super.doExecute();
	}
}