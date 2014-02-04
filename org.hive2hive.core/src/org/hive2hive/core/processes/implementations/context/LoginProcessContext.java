package org.hive2hive.core.processes.implementations.context;

import org.hive2hive.core.model.Locations;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeLocations;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeUserProfile;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideLocations;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideUserProfile;

public class LoginProcessContext implements IProvideUserProfile, IConsumeUserProfile, IProvideLocations,
		IConsumeLocations {

	private UserProfile profile;
	private Locations locations;
	private boolean isMaster;

	@Override
	public void provideUserProfile(UserProfile profile) {
		this.profile = profile;
	}

	@Override
	public UserProfile consumeUserProfile() {
		return profile;
	}

	@Override
	public void provideLocations(Locations locations) {
		this.locations = locations;
	}

	@Override
	public Locations consumeLocations() {
		return locations;
	}

	public void setIsMaster(boolean isMaster) {
		this.isMaster = isMaster;
	}

	public boolean getIsMaster() {
		return isMaster;
	}

}
