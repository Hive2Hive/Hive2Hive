package org.hive2hive.core.process.login;

import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.common.GetUserProfileStep;
import org.hive2hive.core.security.UserPassword;

public class LoginProcess extends Process {

	private UserProfile userProfile;

	public LoginProcess(String userId, String password, String pin, NetworkManager networkManager) {
		super(networkManager);
		UserPassword userPassword = new UserPassword(password, pin);

		// execution order:
		// 1. GetUserProfileStep
		// 2. Verify the user profile
		VerifyUserProfileStep verifyProfileStep = new VerifyUserProfileStep(userId);
		GetUserProfileStep userProfileStep = new GetUserProfileStep(userId, userPassword, verifyProfileStep);
		verifyProfileStep.setPreviousStep(userProfileStep);

		// define first step
		setNextStep(userProfileStep);
	}

	@Override
	protected void finish() {
		// TODO start background process for updating / synchronizing
		super.finish();
	}

	public void setUserProfile(UserProfile userProfile) {
		this.userProfile = userProfile;
	}

	public UserProfile getUserProfile() {
		return userProfile;
	}
}
