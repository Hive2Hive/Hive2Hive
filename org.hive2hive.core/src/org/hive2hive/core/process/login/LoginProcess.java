package org.hive2hive.core.process.login;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.common.get.GetLocationsStep;
import org.hive2hive.core.process.common.get.GetUserProfileStep;
import org.hive2hive.core.security.UserCredentials;

/**
 * Process to log in. This process only logs in. When the credentials match, the locations get updated.
 * This process does <strong>not</strong> synchronize the local files or handle the user message queue.
 * 
 * @author Nico, Christian
 * 
 */
public class LoginProcess extends Process {

	private final LoginProcessContext context;

	public LoginProcess(UserCredentials credentials, NetworkManager networkManager) {
		super(networkManager);
		
		// execution order:
		// 1. GetUserProfileStep
		// 2. VerifyUserProfileStep
		// 3. GetLocationsStep: get the other client's locations
		// 4. AddMyselfToLocationsStep: add this client to the locations map
		AddMyselfToLocationsStep addToLocsStep = new AddMyselfToLocationsStep(credentials.getUserId());
		GetLocationsStep locationsStep = new GetLocationsStep(credentials.getUserId(), addToLocsStep);
		VerifyUserProfileStep verifyProfileStep = new VerifyUserProfileStep(credentials.getUserId(), locationsStep);
		GetUserProfileStep profileStep = new GetUserProfileStep(credentials, verifyProfileStep);
		
		context = new LoginProcessContext(this, profileStep, locationsStep);

		// define first step
		setNextStep(profileStep);
	}

	@Override
	public LoginProcessContext getContext() {
		return context;
	}
}
