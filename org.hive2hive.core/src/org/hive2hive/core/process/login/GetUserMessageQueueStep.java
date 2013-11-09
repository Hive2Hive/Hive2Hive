package org.hive2hive.core.process.login;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.UserMessageQueue;
import org.hive2hive.core.model.UserProfile;
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

	// TODO check where UserMessageQueue is actually stored: userId vs. location based on credentials
	public GetUserMessageQueueStep(UserProfile profile, HandleUserMessageQueueStep handleUmQueueStep) {
		super(profile.getUserId(), H2HConstants.USER_MESSAGE_QUEUE_KEY);

		this.handleUmQueueStep = handleUmQueueStep;
	}

	@Override
	public void start() {

		// check whether this client is master and allowed to execute this step
		if (!((PostLoginProcess) getProcess()).getContext().getIsDefinedAsMaster()) {
			logger.error("This client has not been defined as master client. The UserMessageQueue will not be loaded and this process terminated.");
			getProcess().stop("Client is not master and not allowed to handle the user message queue.");
		}

		// get the UserMessageQueue
		super.start();
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