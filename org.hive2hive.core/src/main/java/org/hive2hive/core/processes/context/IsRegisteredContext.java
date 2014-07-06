package org.hive2hive.core.processes.context;

import org.hive2hive.core.model.Locations;
import org.hive2hive.core.processes.context.interfaces.IGetUserLocationsContext;

public class IsRegisteredContext implements IGetUserLocationsContext {

	private final String userId;

	private Locations locations;

	public IsRegisteredContext(String userId) {
		this.userId = userId;
	}

	@Override
	public void provideUserLocations(Locations locations) {
		this.locations = locations;
	}

	public boolean isRegistered() {
		return locations != null;
	}

	@Override
	public String consumeUserId() {
		return userId;
	}

}
