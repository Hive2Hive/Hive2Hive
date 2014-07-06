package org.hive2hive.core.processes.context;

import java.security.KeyPair;

import org.hive2hive.core.model.Locations;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.processes.context.interfaces.IGetUserLocationsContext;
import org.hive2hive.core.processes.context.interfaces.IPutUserLocationsContext;
import org.hive2hive.core.security.UserCredentials;

public final class RegisterProcessContext implements IGetUserLocationsContext, IPutUserLocationsContext {

	private final UserCredentials userCredentials;

	private Locations locations;
	private UserProfile profile;

	public RegisterProcessContext(UserCredentials userCredentials) {
		this.userCredentials = userCredentials;
	}

	public UserCredentials getUserCredentials() {
		return userCredentials;
	}

	@Override
	public String consumeUserId() {
		return userCredentials.getUserId();
	}

	@Override
	public void provideUserLocations(Locations locations) {
		this.locations = locations;
	}

	@Override
	public Locations consumeUserLocations() {
		return locations;
	}

	public void provideUserProfile(UserProfile profile) {
		this.profile = profile;
	}

	public UserProfile consumeUserProfile() {
		return profile;
	}

	@Override
	public KeyPair consumeUserLocationsProtectionKeys() {
		return profile.getProtectionKeys();
	}

}
