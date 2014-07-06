package org.hive2hive.processframework.util;

import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;

public class BusyFailingStep extends FailingProcessStep {

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {

		// super busy
		TestExecutionUtil.waitDefault();

		super.doExecute();
	}
}
