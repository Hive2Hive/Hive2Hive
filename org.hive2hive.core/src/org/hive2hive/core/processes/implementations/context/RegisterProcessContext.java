package org.hive2hive.core.processes.implementations.context;

import java.security.KeyPair;

import org.hive2hive.core.model.Locations;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeLocations;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeProtectionKeys;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeUserProfile;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideLocations;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideUserProfile;

public final class RegisterProcessContext implements IProvideLocations, IConsumeLocations,
		IConsumeProtectionKeys, IConsumeUserProfile, IProvideUserProfile {

	private Locations locations;
	private UserProfile profile;

	@Override
	public Locations consumeLocations() {
		return locations;
	}

	@Override
	public void provideLocations(Locations locations) {
		this.locations = locations;
	}

	@Override
	public KeyPair consumeProtectionKeys() {
		return profile.getProtectionKeys();
	}

	@Override
	public void provideUserProfile(UserProfile profile) {
		this.profile = profile;
	}

	@Override
	public UserProfile consumeUserProfile() {
		return profile;
	}

}
