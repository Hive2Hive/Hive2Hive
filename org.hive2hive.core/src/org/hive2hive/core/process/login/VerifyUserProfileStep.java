package org.hive2hive.core.process.login;

import net.tomp2p.futures.FutureRemove;

import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.common.get.GetLocationsStep;
import org.hive2hive.core.process.common.get.GetUserProfileStep;

public class VerifyUserProfileStep extends ProcessStep {

	private GetUserProfileStep userProfileStep;
	private final String userId;

	public VerifyUserProfileStep(String userId) {
		this.userId = userId;
	}

	@Override
	public void start() {
		UserProfile userProfile = userProfileStep.getUserProfile();
		if (userProfile == null) {
			// failed for some reason
			getProcess().stop("User profile not found or wrong password");
		} else if (!userProfile.getUserId().equalsIgnoreCase(userId)) {
			// mismatch the userId --> should never happen
			getProcess().stop("UserId does not match the one in the profile");
		} else {
			// 1. GetLocationsStep: get the locations
			// 2. AddMyselfToLocationsStep: add ourself to the location map
			AddMyselfToLocationsStep addToLocsStep = new AddMyselfToLocationsStep(userId);
			GetLocationsStep locationsStep = new GetLocationsStep(userId, addToLocsStep);
			addToLocsStep.setPreviousStep(locationsStep);

			getProcess().setNextStep(locationsStep);
		}
	}

	@Override
	public void rollBack() {
		// do nothing
	}

	@Override
	protected void handleRemovalResult(FutureRemove future) {
		// do nothing
	}

	public void setPreviousStep(GetUserProfileStep userProfileStep) {
		this.userProfileStep = userProfileStep;
	}
}
