package org.hive2hive.core.process.notify.cleanup;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.context.IGetLocationsContext;
import org.hive2hive.core.process.context.ProcessContext;

public class CleanupLocationsProcessContext extends ProcessContext implements IGetLocationsContext {

	private final H2HSession session;
	
	private Locations locations;

	public CleanupLocationsProcessContext(H2HSession session, Process process) {
		super(process);
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
