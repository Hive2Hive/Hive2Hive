package org.hive2hive.core.processes.implementations.register;

import org.hive2hive.core.model.Locations;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideLocations;

public class LocationsCreationStep extends ProcessStep {

	private final String userId;
	private final IProvideLocations locationsContext;

	public LocationsCreationStep(String userId, IProvideLocations locationsContext) {
		this.userId = userId;
		this.locationsContext = locationsContext;
	}
	
	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {

		locationsContext.provideLocations(new Locations(userId));
	}
}
