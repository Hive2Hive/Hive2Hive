package org.hive2hive.core.process.logout;

import org.hive2hive.core.model.Locations;
import org.hive2hive.core.process.context.IGetLocationsContext;
import org.hive2hive.core.process.context.ProcessContext;

public class LogoutProcessContext extends ProcessContext implements IGetLocationsContext {

	public LogoutProcessContext(LogoutProcess logoutProcess) {
		super(logoutProcess);
	}

	private Locations locations;
	
	@Override
	public void setLocation(Locations locations) {
		this.locations = locations;
	}

	@Override
	public Locations getLocations() {
		return locations;
	}

}
