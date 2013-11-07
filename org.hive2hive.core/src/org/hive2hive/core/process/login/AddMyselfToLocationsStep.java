package org.hive2hive.core.process.login;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.model.LocationsEntry;
import org.hive2hive.core.process.common.GetLocationsStep;
import org.hive2hive.core.process.common.PutProcessStep;

public class AddMyselfToLocationsStep extends PutProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(AddMyselfToLocationsStep.class);
	private GetLocationsStep locationsStep;
	private final String userId;

	public AddMyselfToLocationsStep(String userId) {
		super(userId, H2HConstants.USER_LOCATIONS, null, null);
		this.userId = userId;
		// TODO set next step which checks if all peers in the locations are online
	}

	@Override
	public void start() {
		Locations locations = locationsStep.getLocations();
		if (locations == null) {
			logger.error("Something went wrong at the registration: No locations found");
			getProcess().stop("Locations not found");
		} else {
			LocationsEntry myStatus = new LocationsEntry(getNetworkManager().getPeerAddress(), false);
			if (locations.getMaster() == null) {
				// no master exists --> take role of master
				myStatus.setMaster(true);
			}

			locations.addEntry(myStatus);
			put(userId, H2HConstants.USER_LOCATIONS, locations);

			getProcess().setNextStep(null);
		}
	}

	public void setPreviousStep(GetLocationsStep locationsStep) {
		this.locationsStep = locationsStep;
	}

}
