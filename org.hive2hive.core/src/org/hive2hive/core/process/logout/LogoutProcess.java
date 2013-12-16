package org.hive2hive.core.process.logout;

import org.hive2hive.core.exceptions.NoSessionException;
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
	
	public LogoutProcess(NetworkManager networkManager) throws NoSessionException {
		super(networkManager);
		context = new LogoutProcessContext(this);
		
		// execution order
		// 1. GetLocationsStep
		// 2. RemoveOwnLocationStep
		
		RemoveOwnLocationStep removeLocationStep = new RemoveOwnLocationStep(networkManager.getSession().getCredentials().getUserId());
		GetLocationsStep locationsStep = new GetLocationsStep(networkManager.getSession().getCredentials().getUserId(), removeLocationStep, context);
		
		setNextStep(locationsStep);
	}
	
	@Override
	public LogoutProcessContext getContext() {
		return context;
	}

}
