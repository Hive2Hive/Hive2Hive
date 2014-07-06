package org.hive2hive.processframework.util;

import org.hive2hive.processframework.RollbackReason;
import org.hive2hive.processframework.concretes.SequentialProcess;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;

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