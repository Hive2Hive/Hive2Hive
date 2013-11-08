package org.hive2hive.core.process.login;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.model.LocationEntry;
import org.hive2hive.core.process.common.get.GetLocationsStep;
import org.hive2hive.core.process.common.put.PutProcessStep;

/**
 * Verifies the answer of the @link{GetLocationsStep} and adds this node to the list. Note, there is no
 * master-evaluation done here.
 * 
 * @author Nico
 * 
 */
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
			LocationEntry myStatus = new LocationEntry(getNetworkManager().getPeerAddress(), false);
			locations.addEntry(myStatus);
			put(userId, H2HConstants.USER_LOCATIONS, locations);

			((LoginProcess) getProcess()).getContext().setLocations(locations);
			getProcess().setNextStep(null);
		}
	}

	public void setPreviousStep(GetLocationsStep locationsStep) {
		this.locationsStep = locationsStep;
	}

}
