package org.hive2hive.core.process.login;

import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.common.GetUserProfileStep;
import org.hive2hive.core.security.UserPassword;

/**
 * Process to log in. This process only logs in. When the credentials match, the locations get updated.
 * This process does <strong>not</strong> synchronize the local files or handle the user message queue.
 * 
 * @author Nico
 * 
 */
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

	public void setUserProfile(UserProfile userProfile) {
		this.userProfile = userProfile;
	}

	public UserProfile getUserProfile() {
		return userProfile;
	}
}
