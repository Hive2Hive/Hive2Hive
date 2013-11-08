package org.hive2hive.core.process.login;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.LocationEntry;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.process.common.put.PutProcessStep;

/**
 * Verifies the answer of the @link{GetLocationsStep} and adds this client's peer to the locations list. </br>
 * Note, there is no master-evaluation done here.
 * 
 * @author Nico, Christian
 * 
 */
public class AddMyselfToLocationsStep extends PutProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(AddMyselfToLocationsStep.class);

	private final String userId;

	public AddMyselfToLocationsStep(String userId) {
		super(userId, H2HConstants.USER_LOCATIONS, null, null);

		this.userId = userId;
	}

	@Override
	public void start() {

		// get the loaded locations from the process context
		Locations loadedLocations = ((LoginProcess) getProcess()).getContext().getGetLocationsStep()
				.getLocations();

		if (loadedLocations == null) {
			logger.error("Something went wrong at the registration: No locations found.");
			getProcess().stop("Locations not found.");
		} else {

			// add this peer to the locations list and put it
			LocationEntry myLocation = new LocationEntry(getNetworkManager().getPeerAddress(), false);
			loadedLocations.addEntry(myLocation);

			put(userId, H2HConstants.USER_LOCATIONS, loadedLocations);

			// terminate the LoginProcess
			getProcess().terminate();
		}
	}
}
