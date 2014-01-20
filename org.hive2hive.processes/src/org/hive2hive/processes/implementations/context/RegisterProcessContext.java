package org.hive2hive.processes.implementations.context;

import org.hive2hive.core.model.Locations;
import org.hive2hive.processes.implementations.context.interfaces.IConsumeLocations;
import org.hive2hive.processes.implementations.context.interfaces.IProvideLocations;

public final class RegisterProcessContext implements IProvideLocations, IConsumeLocations {

	private Locations locations;

	@Override
	public void provideLocations(Locations locations) {
		this.locations = locations;
	}

	@Override
	public Locations consumeLocations() {
		return locations;
	}

}
