package org.hive2hive.core.process.login;

import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.process.ProcessStep;

/**
 * Verifies the answer of the @link{GetUserProfileStep}. If the user profile is not found or could not be
 * decrypted, the process is stopped. Otherwise, it continues with adding this node to the locations.
 * 
 * @author Nico
 * 
 */
public class VerifyUserProfileStep extends ProcessStep {

	private final String userId;
	private final ProcessStep nextStep;

	public VerifyUserProfileStep(String userId, ProcessStep nextStep) {
		this.userId = userId;
		this.nextStep = nextStep;
	}

	@Override
	public void start() {
		// get the loaded profile from the process context
		UserProfile loadedProfile = ((LoginProcess) getProcess()).getContext().getUserProfile();

		if (loadedProfile == null) {
			// failed for some reason
			getProcess().stop("User profile not found or wrong password.");
		} else if (!loadedProfile.getUserId().equalsIgnoreCase(userId)) {
			// mismatch the userId (should never happen)
			getProcess().stop("UserId does not match the one in the profile.");
		} else {
			getProcess().setNextStep(nextStep);
		}
	}

	@Override
	public void rollBack() {
		getProcess().nextRollBackStep();
	}
}
