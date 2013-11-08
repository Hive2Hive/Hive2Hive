package org.hive2hive.core.process.login;

import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.common.get.GetLocationsStep;

/**
 * Verifies the answer of the @link{GetUserProfileStep}. If the user profile is not found or could not be
 * decrypted, the process is stopped. Otherwise, it continues with adding this node to the locations.
 * 
 * @author Nico
 * 
 */
public class VerifyUserProfileStep extends ProcessStep {

	private final String userId;
	private final GetLocationsStep locationsStep;

	public VerifyUserProfileStep(String userId, GetLocationsStep locationsStep) {
		
		this.userId = userId;
		this.locationsStep = locationsStep;
	}

	@Override
	public void start() {
		
		// get the loaded profile from the process context
		UserProfile loadedProfile = ((LoginProcess)getProcess()).getContext().getGetUserProfileStep().getUserProfile();
		
		if (loadedProfile == null) {
			// failed for some reason
			getProcess().stop("User profile not found or wrong password.");
		} else if (!loadedProfile.getUserId().equalsIgnoreCase(userId)) {
			// mismatch the userId (should never happen)
			getProcess().stop("UserId does not match the one in the profile.");
		} else {

			// TODO check whether this step setting is necessary here. Alternative: only parent-process knows next
			// step and this GetUserProfileStep calls getProcess().stop() and initiates a rollback
			getProcess().setNextStep(locationsStep);
		}
	}

	@Override
	public void rollBack() {
		// do nothing
	}
}
