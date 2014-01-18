package org.hive2hive.processes.implementations.register;

import org.hive2hive.processes.framework.abstracts.RollbackReason;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.processes.implementations.common.BasePutProcessStep;

public class PutUserProfileStep extends BasePutProcessStep {

	@Override
	protected void doExecute() throws InvalidProcessStateException {

		// encrypt user profile
		
		// put encrypted user profile
	}

	@Override
	protected void doPause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doResumeExecution() throws InvalidProcessStateException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doResumeRollback() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doRollback(RollbackReason reason) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPutSuccess() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPutFailure() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRemoveSuccess() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRemoveFailure() {
		// TODO Auto-generated method stub
		
	}


}
