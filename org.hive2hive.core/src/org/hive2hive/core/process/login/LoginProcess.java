package org.hive2hive.core.process.login;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.common.GetUserProfileStep;
import org.hive2hive.core.security.UserPassword;

public class LoginProcess extends Process {

	private final String userId;
	private final UserPassword userPassword;

	public LoginProcess(String userId, String password, String pin, NetworkManager networkManager) {

		super(networkManager);

		this.userId = userId;
		this.userPassword = new UserPassword(password, pin);

		// execution order:
		// 1. GetUserProfileStep
		// 2. Verify the user profile
		VerifyUserProfileStep verifyProfileStep = new VerifyUserProfileStep(userId);
		GetUserProfileStep userProfileStep = new GetUserProfileStep(this.userId, this.userPassword,
				verifyProfileStep);
		verifyProfileStep.setPreviousStep(userProfileStep);

		// define first step
		setNextStep(userProfileStep);
	}

	@Override
	protected void finish() {
		// TODO start background process for updating / synchronizing
		super.finish();
	}

	public String getUserId() {
		return userId;
	}

	public UserPassword getUserPassword() {
		return userPassword;
	}
}
