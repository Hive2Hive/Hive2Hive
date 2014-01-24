package org.hive2hive.processes.implementations.context;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.processes.implementations.context.interfaces.IConsumeLocations;
import org.hive2hive.processes.implementations.context.interfaces.IConsumeSession;
import org.hive2hive.processes.implementations.context.interfaces.IConsumeUserProfile;
import org.hive2hive.processes.implementations.context.interfaces.IProvideLocations;
import org.hive2hive.processes.implementations.context.interfaces.IProvideSession;
import org.hive2hive.processes.implementations.context.interfaces.IProvideUserProfile;

public class LoginProcessContext implements IProvideUserProfile, IConsumeUserProfile, IProvideSession,
		IConsumeSession, IProvideLocations, IConsumeLocations {

	private UserProfile profile;
	private H2HSession session;
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
	public void provideSession(H2HSession session) {
		this.session = session;
	}

	@Override
	public H2HSession consumeSession() {
		return session;
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
