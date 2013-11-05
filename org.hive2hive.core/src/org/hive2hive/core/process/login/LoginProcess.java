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
		userPassword = new UserPassword(password, pin);

		AddMyselfToLocationsStep addToLocsStep = new AddMyselfToLocationsStep(userId);
		GetLocationsStep locationsStep = new GetLocationsStep(userId, addToLocsStep);
		addToLocsStep.setPreviousStep(locationsStep);

		GetUserProfileStep userProfileStep = new GetUserProfileStep(userId, userPassword, locationsStep);
		setNextStep(userProfileStep);
	}

	public String getUserId() {
		return userId;
	}

	public UserPassword getUserPassword() {
		return userPassword;
	}
}
