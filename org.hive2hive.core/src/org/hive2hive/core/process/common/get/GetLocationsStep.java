package org.hive2hive.core.process.common.get;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.context.IGetLocationsContext;

/**
 * Generic process step to get the {@link: UserProfile} and decrypt it. It is then accessible in
 * 
 * @author Nico, Christian
 * 
 */
public class GetLocationsStep extends BaseGetProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(GetLocationsStep.class);

	private final ProcessStep nextStep;
	private IGetLocationsContext context;

	public GetLocationsStep(String userId, ProcessStep nextStep, IGetLocationsContext context) {
		super(userId, H2HConstants.USER_LOCATIONS);
		this.nextStep = nextStep;
		this.context = context;
	}

	@Override
	public void handleGetResult(NetworkContent content) {

		if (content == null) {
			logger.debug("Did not find the locations.");
		} else {
			context.setLocation((Locations) content);
		}

		// TODO check whether this step setting is necessary here. Alternative: only parent-process knows next
		// step and this GetUserProfileStep calls getProcess().stop() and initiates a rollback
		getProcess().setNextStep(nextStep);
	}
}
