package org.hive2hive.processes.implementations.register;

import java.security.KeyPair;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.Locations;
import org.hive2hive.processes.framework.RollbackReason;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.processes.implementations.common.BasePutProcessStep;

public class PutUserLocationsStep extends BasePutProcessStep {

	private final Locations locations;
	private final KeyPair protectionKeys;

	public PutUserLocationsStep(Locations locations, KeyPair protectionKeys) {
		this.protectionKeys = protectionKeys;
		this.locations = locations;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		
		locations.setBasedOnKey(locations.getVersionKey());
		locations.generateVersionKey();
		
		put(locations.getUserId(), H2HConstants.USER_LOCATIONS, locations, protectionKeys);
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
