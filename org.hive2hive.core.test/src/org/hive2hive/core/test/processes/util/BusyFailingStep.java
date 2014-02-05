package org.hive2hive.core.test.processes.util;

import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;

public class BusyFailingStep extends FailingProcessStep {

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		
		// super busy
		TestUtil.wait(3000);

		super.doExecute();
	}
}
