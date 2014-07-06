package org.hive2hive.core.processes.util;

import org.hive2hive.core.model.Locations;
import org.hive2hive.core.processes.context.interfaces.IGetUserLocationsContext;

public class GetUserLocationsContext implements IGetUserLocationsContext {

	private final String userId;

	private Locations locations;

	public GetUserLocationsContext(String userId) {
		this.userId = userId;
	}

	@Override
	public String consumeUserId() {
		return userId;
	}

	@Override
	public void provideUserLocations(Locations locations) {
		this.locations = locations;
	}

	public Locations consumeUserLocations() {
		return locations;
	}

}
