package org.hive2hive.core.processes.implementations.context;

import org.hive2hive.core.model.Locations;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeLocations;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeUserProfile;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideLocations;

public final class RegisterProcessContext implements IProvideLocations, IConsumeLocations, IConsumeUserProfile {

	private Locations locations;
	private final UserProfile profile;
	
	public RegisterProcessContext(UserProfile profile) {
		this.profile = profile;
	}

	@Override
	public Locations consumeLocations() {
		return locations;
	}

	@Override
	public UserProfile consumeUserProfile() {
		return profile;
	}

	@Override
	public void provideLocations(Locations locations) {
		this.locations = locations;
	}

}
