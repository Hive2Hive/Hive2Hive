package org.hive2hive.processes.implementations.register;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.processes.framework.RollbackReason;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.processes.implementations.common.BaseGetProcessStep;

public class GetUserLocationsStep extends BaseGetProcessStep {

	private final String userId;
	private final IProvideLocations context;
	
	private NetworkContent loadedLocations;
	
	public GetUserLocationsStep(String userId, IProvideLocations context) {
		this.userId = userId;
		this.context = context;
	}
	
	@Override
	protected void doExecute() throws InvalidProcessStateException {

		get(userId, H2HConstants.USER_LOCATIONS);
		
		// wait (blocking) for result
		while(loadedLocations == null){
			// TODO optimize busy wait (latch)
		}
		
		// TODO check type
		if (loadedLocations instanceof Locations) {
			context.provideLocations((Locations) loadedLocations);
		} else {
			// cancel process
			cancel(new RollbackReason(this, "Loaded wrong type."));
		}
		
	}

	@Override
	public void handleGetResult(NetworkContent content) {
		this.loadedLocations = content;
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
		// ignore: only a get was done
	}

}
