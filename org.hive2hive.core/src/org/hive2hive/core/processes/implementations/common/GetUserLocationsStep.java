package org.hive2hive.core.processes.implementations.common;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.common.base.BaseGetProcessStep;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideLocations;

public class GetUserLocationsStep extends BaseGetProcessStep {

	private final String userId;
	private final IProvideLocations context;

	public GetUserLocationsStep(String userId, IProvideLocations context, IDataManager dataManager) {
		super(dataManager);
		this.userId = userId;
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		NetworkContent loadedContent = get(userId, H2HConstants.USER_LOCATIONS);

		if (loadedContent == null) {
			context.provideLocations(null);
		} else {
			context.provideLocations((Locations) loadedContent);
		}

	}

}
