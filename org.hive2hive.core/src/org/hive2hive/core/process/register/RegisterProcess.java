package org.hive2hive.core.process.register;

import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.common.get.GetLocationsStep;
import org.hive2hive.core.security.UserCredentials;

public class RegisterProcess extends Process {

	private final RegisterProcessContext context;

	public RegisterProcess(UserCredentials credentials, NetworkManager networkManager) {
		super(networkManager);

		// create and set context
		UserProfile userProfile = new UserProfile(credentials.getUserId());
		context = new RegisterProcessContext(this, credentials, userProfile);

		// get the locations map to check if a user with the same name is already existent
		CheckIfUserExistsStep userExistsStep = new CheckIfUserExistsStep();
		GetLocationsStep getLocationsStep = new GetLocationsStep(credentials.getUserId(), userExistsStep,
				context);
		setNextStep(getLocationsStep);
	}

	@Override
	public RegisterProcessContext getContext() {
		return context;
	}
}
