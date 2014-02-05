package org.hive2hive.core.test.processes.util;

import org.hive2hive.core.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.concretes.SequentialProcess;

public class FailingSequentialProcess extends SequentialProcess {
	
	@Override
	protected void doExecute() throws InvalidProcessStateException {
		super.doExecute();
		
		cancel(new RollbackReason(this, "Test process that must fail."));
	}
	
	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {
		super.doRollback(reason);
	}
}
