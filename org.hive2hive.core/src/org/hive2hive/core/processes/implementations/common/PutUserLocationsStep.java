package org.hive2hive.core.processes.implementations.common;

import java.io.IOException;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.implementations.common.base.BasePutProcessStep;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeLocations;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeUserProfile;

public class PutUserLocationsStep extends BasePutProcessStep {

	private final IConsumeLocations locationsContext;
	private final IConsumeUserProfile profileContext;

	public PutUserLocationsStep(IConsumeLocations locationsContext, IConsumeUserProfile profileContext,
			IDataManager dataManager) {
		super(dataManager);
		this.locationsContext = locationsContext;
		this.profileContext = profileContext;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		Locations locations = locationsContext.consumeLocations();

		locations.setBasedOnKey(locations.getVersionKey());
		try {
			locations.generateVersionKey();
		} catch (IOException e) {
			cancel(new RollbackReason(this, "Could not generate version key."));
			return;
		}

		try {
			put(locations.getUserId(), H2HConstants.USER_LOCATIONS, locations, profileContext
					.consumeUserProfile().getProtectionKeys());
		} catch (PutFailedException e) {
			cancel(new RollbackReason(this, "Put failed."));
		}
	}

}
