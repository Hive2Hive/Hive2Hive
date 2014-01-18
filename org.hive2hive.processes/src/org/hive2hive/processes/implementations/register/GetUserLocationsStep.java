package org.hive2hive.processes.implementations.register;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.processes.framework.RollbackReason;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.processes.implementations.common.BaseGetProcessStep;

public class GetUserLocationsStep extends BaseGetProcessStep {

	private final String userId;
	private final IProvideLocations context;
	
	private boolean isPutCompleted;
	private NetworkContent loadedContent;
	
	public GetUserLocationsStep(String userId, IProvideLocations context, NetworkManager networkManager) {
		super (networkManager);
		this.userId = userId;
		this.context = context;
	}
	
	@Override
	protected void doExecute() throws InvalidProcessStateException {

		get(userId, H2HConstants.USER_LOCATIONS);
		
		// wait for GET to complete
		while(isPutCompleted == false) {
			// TODO optimize busy wait (latch)
		}
		
		if (loadedContent == null) {
			context.provideLocations(null);
		} else {
			context.provideLocations((Locations) loadedContent);
		}
		
	}

	@Override
	public void handleGetResult(NetworkContent content) {
		isPutCompleted = true;
		this.loadedContent = content;
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
