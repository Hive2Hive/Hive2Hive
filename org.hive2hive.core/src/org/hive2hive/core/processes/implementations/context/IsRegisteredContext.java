package org.hive2hive.core.processes.implementations.context;

import org.hive2hive.core.model.Locations;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideLocations;

public class IsRegisteredContext implements IProvideLocations {

	private Locations locations;

	@Override
	public void provideLocations(Locations locations) {
		this.locations = locations;
	}

	public boolean isRegistered() {
		return locations != null;
	}

}
