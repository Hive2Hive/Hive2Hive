package org.hive2hive.processes.implementations.register;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.processes.framework.abstracts.RollbackReason;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.processes.implementations.common.BaseGetProcessStep;

public class GetUserLocationsStep extends BaseGetProcessStep {

	private NetworkContent loadedLocations;
	private IProvideLocations context;
	
	public GetUserLocationsStep(String userId, IProvideLocations context) {
		super(userId, H2HConstants.USER_LOCATIONS);
		this.context = context;
	}
	
	@Override
	protected void doExecute() throws InvalidProcessStateException {
		super.doExecute();
		
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

}
