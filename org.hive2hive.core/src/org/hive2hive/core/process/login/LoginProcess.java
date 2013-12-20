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

	public LoginProcess(UserCredentials credentials, SessionParameters sessionParameters,
			NetworkManager networkManager) {
		super(networkManager);
		context = new LoginProcessContext(this);

		// execution order:
		// 1. GetUserProfileStep
		// 2. VerifyUserProfileStep
		// SessionCreationStep
		// 3. GetLocationsStep: get the other client's locations
		// 4. AddMyselfToLocationsStep: add this client to the locations map

		// 5. ContactPeersStep (-> PutLocationsStep)
		// 6. SynchronizeFilesStep
		// if elected master:
		// 7. HandleUserMessageQueueStep

		// TODO add myself to locations here or in PostLoginProcess?

		AddMyselfToLocationsStep addToLocsStep = new AddMyselfToLocationsStep(credentials.getUserId(),
				new ContactPeersStep());

		GetLocationsStep locationsStep = new GetLocationsStep(credentials.getUserId(), addToLocsStep, context);

		SessionCreationStep sessionStep = new SessionCreationStep(sessionParameters, networkManager,
				locationsStep);

		VerifyUserProfileStep verifyProfileStep = new VerifyUserProfileStep(credentials.getUserId(),
				sessionStep);

		GetUserProfileStep profileStep = new GetUserProfileStep(credentials, context, verifyProfileStep);

		// define first step
		setNextStep(profileStep);

	}

	@Override
	public LoginProcessContext getContext() {
		return context;
	}
}
