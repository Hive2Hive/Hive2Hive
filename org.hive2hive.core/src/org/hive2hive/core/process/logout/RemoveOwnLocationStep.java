package org.hive2hive.core.process.logout;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.process.common.put.BasePutProcessStep;

/**
 * Verifies the answer of the @link{GetLocationsStep} and removes this client's peer address from the locations list. </br>
 * Note, there is no master-evaluation done here.
 * 
 * @author Christian
 * 
 */
public class RemoveOwnLocationStep extends BasePutProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(RemoveOwnLocationStep.class);
	
	private String userId;

	public RemoveOwnLocationStep(String userId) {
		super(null);  // terminates process after this step
		this.userId = userId;
	}

	@Override
	public void start() {

		// get the loaded locations from the process context
		Locations loadedLocations = ((LogoutProcess) getProcess()).getContext().getLocations();

		if (loadedLocations == null) {
			logger.error("Something went wrong: No locations found.");
			getProcess().stop("Locations not found.");
		} else {

			// remove this peer from the locations list and put it
			loadedLocations.removePeerAddress(getNetworkManager().getPeerAddress());

			put(userId, H2HConstants.USER_LOCATIONS, loadedLocations);
		}
	}
}
