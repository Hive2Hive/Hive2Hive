package org.hive2hive.core.processes.implementations.common;

import java.io.IOException;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.common.base.BasePutProcessStep;
import org.hive2hive.core.processes.implementations.context.interfaces.common.IPutUserLocationsContext;

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
