package org.hive2hive.core.process.register;

import java.io.IOException;

import net.tomp2p.futures.FutureGet;
import net.tomp2p.futures.FutureRemove;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.common.PutLocationStep;
import org.hive2hive.core.process.common.PutUserProfileStep;
import org.hive2hive.core.security.UserPassword;

public class CheckIfProfileExistsStep extends ProcessStep {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(CheckIfProfileExistsStep.class);
	private final UserProfile profile;
	private final String locationKey;

	public CheckIfProfileExistsStep(UserProfile profile, UserPassword password) {
		this.profile = profile;

		// password is required to get the location key
		this.locationKey = profile.getLocationKey(password);
	}

	@Override
	public void start() {
		logger.debug(String.format("Checking if a user profile already exists. user id = '%s'",
				profile.getUserId()));
		get(locationKey, H2HConstants.USER_PROFILE);
	}

	@Override
	public void rollBack() {
		// only a get call which has no effect
	}

	@Override
	protected void handleMessageReply(ResponseMessage asyncReturnMessage) {
		// not used
	}

	@Override
	protected void handleGetResult(FutureGet future) {
		if (future.getData() == null) {
			logger.debug(String.format("No user profile exists. user id = '%s'", profile.getUserId()));
			continueWithNextStep();
		} else {
			try {
				if (!(future.getData().object() instanceof UserProfile)) {
					logger.warn(String.format("Instance of UserProfile expected. key = '%s'", locationKey));
				}
			} catch (ClassNotFoundException | IOException e) {
				logger.warn(String.format("future.getData().getObject() failed. reason = '%s'",
						e.getMessage()));
			}
			getProcess().rollBack("User profile already exists.");
		}
	}

	private void continueWithNextStep() {
		RegisterProcess process = (RegisterProcess) super.getProcess();
		UserProfile userProfile = process.getUserProfile();

		// create the next steps:
		// first, put the new user profile
		// second, put the empty locations map
		// third, put the public key of the user
		PutPublicKeyStep third = new PutPublicKeyStep(userProfile.getUserId(), userProfile
				.getEncryptionKeys().getPublic());
		PutLocationStep second = new PutLocationStep(new Locations(userProfile.getUserId()), third);
		PutUserProfileStep first = new PutUserProfileStep(userProfile, process.getUserPassword(), second);
		getProcess().nextStep(first);
	}

	@Override
	protected void handleRemovalResult(FutureRemove future) {
		// not used
	}
}
