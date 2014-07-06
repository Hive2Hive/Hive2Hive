package org.hive2hive.core.processes.common;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.model.NetworkContent;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.processes.common.base.BaseGetProcessStep;
import org.hive2hive.core.processes.context.interfaces.IGetUserLocationsContext;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;

public class GetUserLocationsStep extends BaseGetProcessStep {

	private final IGetUserLocationsContext context;

	public GetUserLocationsStep(IGetUserLocationsContext context, IDataManager dataManager) {
		super(dataManager);
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		String userId = context.consumeUserId();
		NetworkContent loadedContent = get(userId, H2HConstants.USER_LOCATIONS);

		if (loadedContent == null) {
			context.provideUserLocations(null);
		} else {
			Locations locations = (Locations) loadedContent;
			if (!locations.getUserId().equalsIgnoreCase(userId)) {
				throw new ProcessExecutionException(String.format(
						"The wrong locations have been loaded. Required: %s. Got: %s.", userId, locations.getUserId()));
			}

			context.provideUserLocations(locations);
		}
	}
}
