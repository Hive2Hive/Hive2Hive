package org.hive2hive.core.process.register;

import net.tomp2p.futures.FutureRemove;

import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.common.get.GetLocationsStep;
import org.hive2hive.core.process.common.put.PutLocationStep;
import org.hive2hive.core.process.common.put.PutUserProfileStep;

/**
 * This step is called as soon as the @link{GetLocationsStep} is done.
 * 
 * @author Nico
 * 
 */
public class CheckIfUserExistsStep extends ProcessStep {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(CheckIfUserExistsStep.class);
	private GetLocationsStep previousStep;

	@Override
	public void start() {
		logger.debug("Checking if a user profile already exists.");
		if (previousStep.getLocations() == null) {
			// ok, does not exist
			continueWithNextStep();
		} else {
			logger.error("User profile already exists");
			getProcess().stop("Profile already exists");
		}
	}

	private void continueWithNextStep() {
		RegisterProcess process = (RegisterProcess) super.getProcess();
		UserProfile userProfile = process.getContext().getUserProfile();

		// create the next steps:
		// first, put the new user profile
		// second, put the empty locations map
		// third, put the public key of the user
		PutPublicKeyStep third = new PutPublicKeyStep(userProfile.getUserId(), userProfile
				.getEncryptionKeys().getPublic());
		PutLocationStep second = new PutLocationStep(new Locations(userProfile.getUserId()), third);
		PutUserProfileStep first = new PutUserProfileStep(userProfile, process.getContext().getUserCredentials(), second);
		getProcess().setNextStep(first);
	}

	@Override
	public void rollBack() {
		// only a get call which has no effect
	}

	@Override
	protected void handleRemovalResult(FutureRemove future) {
		// not used
	}

	public void setPreviousStep(GetLocationsStep locationsStep) {
		previousStep = locationsStep;
	}
}
