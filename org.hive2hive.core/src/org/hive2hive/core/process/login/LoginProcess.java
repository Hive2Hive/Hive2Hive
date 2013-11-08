package org.hive2hive.core.process.login;

import org.hive2hive.core.UserCredentials;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.common.get.GetUserProfileStep;
import org.hive2hive.core.security.UserPassword;

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
		// 2. Verify the user profile
		VerifyUserProfileStep verifyProfileStep = new VerifyUserProfileStep(credentials.getUserId());
		GetUserProfileStep userProfileStep = new GetUserProfileStep(credentials, verifyProfileStep);
		verifyProfileStep.setPreviousStep(userProfileStep);

		// define first step
		setNextStep(userProfileStep);
	}

	@Override
	public LoginProcessContext getContext() {
		return context;
	}
}
