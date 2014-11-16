package org.hive2hive.core.processes.register;

import org.hive2hive.core.model.versioned.Locations;
import org.hive2hive.core.processes.context.RegisterProcessContext;
import org.hive2hive.processframework.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocationsCreationStep extends ProcessStep<Void> {

	private static final Logger logger = LoggerFactory.getLogger(LocationsCreationStep.class);

	private final RegisterProcessContext context;

	public LocationsCreationStep(RegisterProcessContext context) {
		this.setName(getClass().getName());
		this.context = context;
	}

	@Override
	protected Void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		String userId = context.consumeUserId();
		logger.trace("Creating new user locations list. user id ='{}'", userId);
		context.provideUserLocations(new Locations(userId));
		return null;
	}
}
