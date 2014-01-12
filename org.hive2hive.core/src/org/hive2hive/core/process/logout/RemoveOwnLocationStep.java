package org.hive2hive.core.process.logout;

import java.security.KeyPair;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.process.common.put.BasePutProcessStep;

/**
 * Verifies the answer of the @link{GetLocationsStep} and removes this client's peer address from the
 * locations list. </br>
 * Note, there is no master-evaluation done here.
 * 
 * @author Christian
 * 
 */
public class RemoveOwnLocationStep extends BasePutProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(RemoveOwnLocationStep.class);

	public RemoveOwnLocationStep() {
		super(null); // terminates process after this step
	}

	@Override
	public void start() {
		LogoutProcessContext context = (LogoutProcessContext) getProcess().getContext();
		
		// get the loaded locations from the process context
		Locations loadedLocations = context.getLocations();

		if (loadedLocations == null) {
			logger.error("Something went wrong: No locations found.");
			getProcess().stop("Locations not found.");
		} else {
			String userId = context.getH2HSession().getCredentials().getUserId();
			KeyPair protectionKeys;
			try {
				protectionKeys = context.getH2HSession().getProfileManager().getDefaultProtectionKey();
			} catch (GetFailedException e) {
				getProcess().stop(e);
				return;
			}

			// remove this peer from the locations list and put it
			loadedLocations.removePeerAddress(getNetworkManager().getPeerAddress());
			loadedLocations.setBasedOnKey(loadedLocations.getVersionKey());
			loadedLocations.generateVersionKey();
			put(userId, H2HConstants.USER_LOCATIONS, loadedLocations, protectionKeys);
		}
	}
}
