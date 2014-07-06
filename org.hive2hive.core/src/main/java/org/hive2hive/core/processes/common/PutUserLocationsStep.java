package org.hive2hive.core.processes.common;

import java.io.IOException;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.processes.common.base.BasePutProcessStep;
import org.hive2hive.core.processes.context.interfaces.IPutUserLocationsContext;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;

public class PutUserLocationsStep extends BasePutProcessStep {

	private final IPutUserLocationsContext context;

	public PutUserLocationsStep(IPutUserLocationsContext context, IDataManager dataManager) {
		super(dataManager);
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		Locations locations = context.consumeUserLocations();

		locations.setBasedOnKey(locations.getVersionKey());
		try {
			locations.generateVersionKey();
		} catch (IOException e) {
			throw new ProcessExecutionException("Could not generate version key.", e);
		}

		try {
			put(locations.getUserId(), H2HConstants.USER_LOCATIONS, locations, context.consumeUserLocationsProtectionKeys());
		} catch (PutFailedException e) {
			throw new ProcessExecutionException(e);
		}
	}

}
