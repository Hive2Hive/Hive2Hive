package org.hive2hive.core.process.context;

import org.hive2hive.core.model.Locations;

public interface IGetLocationsContext {

	void setLocations(Locations locations);

	Locations getLocations();
}
