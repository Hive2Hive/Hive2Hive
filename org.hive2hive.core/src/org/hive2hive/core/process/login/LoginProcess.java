package org.hive2hive.core.process.login;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.common.GetLocationsStep;
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
		// 2. GetLocationsStep
		// 3. AddMyselfToLocationsStep
		AddMyselfToLocationsStep addToLocsStep = new AddMyselfToLocationsStep(userId);
		GetLocationsStep locationsStep = new GetLocationsStep(userId, addToLocsStep);
		addToLocsStep.setPreviousStep(locationsStep);
		GetUserProfileStep userProfileStep = new GetUserProfileStep(this.userId, this.userPassword, locationsStep);
		
		// define first step
		setNextStep(userProfileStep);
	}

	public String getUserId() {
		return userId;
	}

	public UserPassword getUserPassword() {
		return userPassword;
	}
}
