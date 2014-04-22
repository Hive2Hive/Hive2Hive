package org.hive2hive.core.processes.util;

import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;

public class BusyFailingStep extends FailingProcessStep {

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		
		// super busy
		TestUtil.waitDefault();

		super.doExecute();
	}
}
