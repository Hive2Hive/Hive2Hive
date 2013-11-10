package org.hive2hive.core.process.login;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.UserMessageQueue;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.process.common.get.BaseGetProcessStep;

/**
 * This step is only important for a master client that has to handle all the buffered user messages.
 * 
 * @author Christian
 * 
 */
public class GetUserMessageQueueStep extends BaseGetProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(GetUserMessageQueueStep.class);

	private final HandleUserMessageQueueStep handleUmQueueStep;
	private UserMessageQueue umQueue;

	public GetUserMessageQueueStep(String locationKey, HandleUserMessageQueueStep handleUmQueueStep) {
		super(locationKey, H2HConstants.USER_MESSAGE_QUEUE_KEY);

		this.handleUmQueueStep = handleUmQueueStep;
	}

	@Override
	protected void handleGetResult(NetworkContent content) {

		if (content == null) {
			logger.debug("Did not find the user message queue.");
		} else {
			umQueue = (UserMessageQueue) content;
		}

		// TODO check whether this step setting is necessary here. Alternative: only parent-process knows next
		// step and this GetUserMessageQueueStep calls getProcess().stop() and initiates a rollback
		getProcess().setNextStep(handleUmQueueStep);
	}

	public UserMessageQueue getUserMessageQueue() {
		return umQueue;
	}
}