package org.hive2hive.core.process.logout;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.process.context.IGetLocationsContext;
import org.hive2hive.core.process.context.ProcessContext;

public class LogoutProcessContext extends ProcessContext implements IGetLocationsContext {

	private final H2HSession session;
	
	private Locations locations;

	public LogoutProcessContext(H2HSession session, LogoutProcess logoutProcess) {
		super(logoutProcess);
		this.session = session;
	}

	public H2HSession getH2HSession() {
		return session;
	}
	
	@Override
	public void setLocations(Locations locations) {
		this.locations = locations;
	}

	@Override
	public Locations getLocations() {
		return locations;
	}

}
