package org.hive2hive.core.process.logout;

import org.hive2hive.core.H2HSession;
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

		H2HSession session = networkManager.getSession();
		context = new LogoutProcessContext(session, this);

		// execution order
		// 1. GetLocationsStep
		// 2. RemoveOwnLocationStep
		RemoveOwnLocationStep removeLocationStep = new RemoveOwnLocationStep();
		GetLocationsStep locationsStep = new GetLocationsStep(session.getCredentials().getUserId(),
				removeLocationStep, context);

		setNextStep(locationsStep);
	}

	@Override
	public LogoutProcessContext getContext() {
		return context;
	}

}
