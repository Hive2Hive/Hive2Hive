package org.hive2hive.processes.test.util;

import org.hive2hive.core.model.Locations;
import org.hive2hive.processes.implementations.context.interfaces.IProvideLocations;

public class GetUserLocationsContext implements IProvideLocations {

	public Locations locations;

	@Override
	public void provideLocations(Locations locations) {
		this.locations = locations;
	}

}
