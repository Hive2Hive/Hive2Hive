package org.hive2hive.processes.implementations.context;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.model.Locations;
import org.hive2hive.processes.implementations.context.interfaces.IConsumeLocations;
import org.hive2hive.processes.implementations.context.interfaces.IConsumeSession;
import org.hive2hive.processes.implementations.context.interfaces.IProvideLocations;

public class LogoutProcessContext implements IProvideLocations, IConsumeLocations, IConsumeSession {

	private final H2HSession session;
	private Locations locations;

	public LogoutProcessContext(H2HSession session) {
		this.session = session;
	}

	@Override
	public void provideLocations(Locations locations) {
		this.locations = locations;
	}

	@Override
	public Locations consumeLocations() {
		return locations;
	}

	@Override
	public H2HSession consumeSession() {
		return session;
	}
}
