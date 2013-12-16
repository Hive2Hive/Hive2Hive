package org.hive2hive.core.process.logout;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.common.get.GetLocationsStep;

/**
 * Process to log out. Removes this client's peer address from the locations list.
 * 
 * @author Christian
 * 
 */
public class LogoutProcess extends Process {

	private final LogoutProcessContext context;
	
	public LogoutProcess(String userId, NetworkManager networkManager) {
		super(networkManager);
		context = new LogoutProcessContext(this);
		
		// execution order
		// 1. GetLocationsStep
		// 2. RemoveOwnLocationStep
		
		RemoveOwnLocationStep removeLocationStep = new RemoveOwnLocationStep(userId);
		GetLocationsStep locationsStep = new GetLocationsStep(userId, removeLocationStep, context);
		
		setNextStep(locationsStep);
	}
	
	@Override
	public LogoutProcessContext getContext() {
		return context;
	}

}
