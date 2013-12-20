package org.hive2hive.core.process.login;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.common.put.BasePutProcessStep;

/**
 * Verifies the answer of the @link{GetLocationsStep} and adds this client's peer to the locations list. </br>
 * Note, there is no master-evaluation done here.
 * 
 * @author Nico, Christian
 * 
 */
public class AddMyselfToLocationsStep extends BasePutProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(AddMyselfToLocationsStep.class);

	private final String userId;

	public AddMyselfToLocationsStep(String userId, ProcessStep nextStep) {
		super(nextStep);

		this.userId = userId;
	}

	@Override
	public void start() {

		// get the loaded locations from the process context
		Locations loadedLocations = ((LoginProcess) getProcess()).getContext().getLocations();

		if (loadedLocations == null) {
			logger.error("Locations not found.");
			getProcess().stop("Locations not found.");
		} else {

			// add this peer to the locations list and put it
			loadedLocations.addPeerAddress(getNetworkManager().getPeerAddress());

			put(userId, H2HConstants.USER_LOCATIONS, loadedLocations);

			// terminate the LoginProcess
			getProcess().terminate();
		}
	}
}
