package org.hive2hive.core.process.common.get;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.context.IGetLocationsContext;

/**
 * Generic process step to get the {@link: Locations}. It is then accessible in the context
 * object
 * 
 * @author Nico, Christian, Seppi
 * 
 */
public class GetLocationsStep extends BaseGetProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(GetLocationsStep.class);

	private final String userId;
	private final ProcessStep nextStep;
	private IGetLocationsContext context;

	public GetLocationsStep(String userId, ProcessStep nextStep, IGetLocationsContext context) {
		this.userId = userId;
		this.nextStep = nextStep;
		this.context = context;
	}

	@Override
	public void start() {
		logger.debug("Get the locations for user '" + userId + "'.");
		NetworkContent content = get(userId, H2HConstants.USER_LOCATIONS);

		if (content == null) {
			logger.warn("Did not find the locations.");
			context.setLocations(null);
		} else {
			context.setLocations((Locations) content);
		}
		getProcess().setNextStep(nextStep);
	}
}
