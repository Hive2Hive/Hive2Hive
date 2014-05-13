package org.hive2hive.core.processes.implementations.context.interfaces.common;

import org.hive2hive.core.model.Locations;

public interface IGetUserLocationsContext {

	public String consumeUserId();
	
	public void provideUserLocations(Locations locations);

}
