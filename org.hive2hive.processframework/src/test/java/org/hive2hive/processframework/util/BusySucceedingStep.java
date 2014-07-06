package org.hive2hive.processframework.util;

import org.hive2hive.processframework.exceptions.InvalidProcessStateException;

public class BusySucceedingStep extends SucceedingProcessStep {

	@Override
	protected void doExecute() throws InvalidProcessStateException {

		// super busy
		TestExecutionUtil.waitDefault();

		super.doExecute();
	}
}