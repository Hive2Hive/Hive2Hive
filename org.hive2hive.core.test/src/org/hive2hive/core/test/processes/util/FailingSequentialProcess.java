package org.hive2hive.core.test.processes.util;

import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.concretes.SequentialProcess;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;

public class FailingSequentialProcess extends SequentialProcess {
	
	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		super.doExecute();
	
		throw new ProcessExecutionException("Test process that must fail.");
	}
	
	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {
		super.doRollback(reason);
	}
}