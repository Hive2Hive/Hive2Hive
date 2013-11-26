package org.hive2hive.core.process.common.get;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.process.ProcessStep;

/**
 * This step is only important for a master client that has to handle all the buffered user messages.
 * 
 * @author Christian
 * 
 */
@Deprecated
public class GetUserMessageQueueStep extends BaseGetProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(GetUserMessageQueueStep.class);

	private final String userId;
	private final ProcessStep nextStep;
//	private UserMessageQueue umQueue;

	public GetUserMessageQueueStep(String userId, ProcessStep nextStep) {
		this.userId = userId;
		this.nextStep = nextStep;
	}
	
	@Override
	public void start() {
		get(userId, H2HConstants.USER_MESSAGE_QUEUE_KEY);
	}

	@Override
	public void handleGetResult(NetworkContent content) {
		if (content == null) {
			logger.debug("Did not find the user message queue.");
		} else {
//			umQueue = (UserMessageQueue) content;
		}

		// TODO check whether this step setting is necessary here. Alternative: only parent-process knows next
		// step and this GetUserMessageQueueStep calls getProcess().stop() and initiates a rollback
		getProcess().setNextStep(nextStep);
	}
}