package org.hive2hive.core.process.login;

import org.hive2hive.core.model.Locations;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.process.ProcessContext;

public final class LoginProcessContext extends ProcessContext {

	private UserProfile userProfile;
	private Locations locations;
	
	public LoginProcessContext(LoginProcess loginProcess) {
		super(loginProcess);
	}
	
	public void setUserProfile(UserProfile userProfile) {
		this.userProfile = userProfile;
	}

	public UserProfile getUserProfile() {
		return userProfile;
	}

	public void setLocations(Locations locations) {
		this.locations = locations;
	}
	
	public Locations getLocations() {
		return locations;
	}
}
