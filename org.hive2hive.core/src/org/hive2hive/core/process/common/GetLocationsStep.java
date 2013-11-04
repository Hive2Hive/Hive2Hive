package org.hive2hive.core.process.common;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.process.ProcessStep;

/**
 * Generic process step to get the {@link: UserProfile} and decrypt it. It is then accessible in
 * 
 * @author Nico
 * 
 */
public class GetLocationsStep extends GetProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(GetLocationsStep.class);
	private final ProcessStep nextStep;
	private Locations locations;

	public GetLocationsStep(String userId, ProcessStep nextStep) {
		super(userId, H2HConstants.USER_LOCATIONS);
		this.nextStep = nextStep;
	}

	@Override
	protected void handleGetResult(NetworkContent content) {
		if (content == null) {
			logger.debug("Did not find the locations");
		} else {
			locations = (Locations) content;
		}

		getProcess().nextStep(nextStep);
	}

	public Locations getLocations() {
		return locations;
	}
}
