package org.hive2hive.processes.contexts;

import org.hive2hive.core.model.Locations;
import org.hive2hive.processes.contexts.interfaces.common.IGetLocations;

public class RegisterContext implements IGetLocations {

	private Locations locations;
	
	@Override
	public Locations getLocations() {
		return locations;
	}

}
