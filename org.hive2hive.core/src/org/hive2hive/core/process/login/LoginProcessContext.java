package org.hive2hive.core.process.login;

import org.hive2hive.core.model.Locations;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.process.context.IGetLocationsContext;
import org.hive2hive.core.process.context.IGetUserProfileContext;
import org.hive2hive.core.process.context.ProcessContext;

public final class LoginProcessContext extends ProcessContext implements IGetLocationsContext,
		IGetUserProfileContext {

	private Locations locations;
	private UserProfile userProfile;

	public LoginProcessContext(LoginProcess loginProcess) {
		super(loginProcess);
	}

	@Override
	public void setLocation(Locations locations) {
		this.locations = locations;
	}

	@Override
	public Locations getLocations() {
		return locations;
	}

	@Override
	public void setUserProfile(UserProfile userProfile) {
		this.userProfile = userProfile;
	}

	@Override
	public UserProfile getUserProfile() {
		return userProfile;
	}
}
