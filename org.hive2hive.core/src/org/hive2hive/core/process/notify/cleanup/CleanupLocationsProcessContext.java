package org.hive2hive.core.process.notify.cleanup;

import org.hive2hive.core.model.Locations;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.context.IGetLocationsContext;
import org.hive2hive.core.process.context.ProcessContext;

public class CleanupLocationsProcessContext extends ProcessContext implements IGetLocationsContext {

	private Locations locations;

	public CleanupLocationsProcessContext(Process process) {
		super(process);
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
