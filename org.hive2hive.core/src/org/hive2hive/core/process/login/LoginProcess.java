package org.hive2hive.core.process.login;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.common.get.GetLocationsStep;
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
		context = new LoginProcessContext(this);

		// execution order:
		// 1. GetUserProfileStep
		// 2. VerifyUserProfileStep
		// 3. GetPublicKeyStep
		// 4. GetLocationsStep: get the other client's locations
		// 5. AddMyselfToLocationsStep: add this client to the locations map

		// TODO add myself to locations here or in PostLoginProcess?
		AddMyselfToLocationsStep addToLocsStep = new AddMyselfToLocationsStep(credentials.getUserId());
		GetLocationsStep locationsStep = new GetLocationsStep(credentials.getUserId(), addToLocsStep, context);
		VerifyUserProfileStep verifyProfileStep = new VerifyUserProfileStep(credentials.getUserId(),
				locationsStep);
		GetUserProfileStep profileStep = new GetUserProfileStep(credentials, context, verifyProfileStep);

		// define first step
		setNextStep(profileStep);
	}

	@Override
	public LoginProcessContext getContext() {
		return context;
	}
}
