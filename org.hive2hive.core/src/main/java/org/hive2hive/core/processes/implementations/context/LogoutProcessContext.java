package org.hive2hive.core.processes.implementations.context;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.processes.implementations.context.interfaces.common.IGetUserLocationsContext;

public class LogoutProcessContext implements IGetUserLocationsContext {

	private final H2HSession session;
	private Locations locations;

	public LogoutProcessContext(H2HSession session) {
		this.session = session;
	}

	@Override
	public String consumeUserId() {
		return session.getUserId();
	}

	@Override
	public void provideUserLocations(Locations locations) {
		this.locations = locations;
	}

	public Locations consumeUserLocations() {
		return locations;
	}

	public H2HSession consumeSession() {
		return session;
	}
}
