package org.hive2hive.core.process.register;

import org.hive2hive.core.model.Locations;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.process.context.IGetLocationsContext;
import org.hive2hive.core.process.context.ProcessContext;
import org.hive2hive.core.security.UserCredentials;

public final class RegisterProcessContext extends ProcessContext implements IGetLocationsContext {

	private final UserCredentials userCredentials;
	private final UserProfile userProfile;
	private Locations locations;

	public RegisterProcessContext(RegisterProcess registerProcess, UserCredentials credentials,
			UserProfile profile) {
		super(registerProcess);

		this.userCredentials = credentials;
		this.userProfile = profile;
	}

	public UserCredentials getUserCredentials() {
		return userCredentials;
	}

	public UserProfile getUserProfile() {
		return userProfile;
	}

	@Override
	public void setLocations(Locations locations) {
		this.locations = locations;
	}

	@Override
	public Locations getLocations() {
		return locations;
	}
}
