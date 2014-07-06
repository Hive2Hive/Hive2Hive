package org.hive2hive.core.processes.context.interfaces;

import org.hive2hive.core.model.Locations;

public interface IGetUserLocationsContext {

	public String consumeUserId();
	
	public void provideUserLocations(Locations locations);

}
