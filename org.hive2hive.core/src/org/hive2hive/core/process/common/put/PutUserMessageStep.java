package org.hive2hive.core.process.common.put;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.network.data.NetworkContentWrapper;
import org.hive2hive.core.network.messages.BaseMessage;
import org.hive2hive.core.process.ProcessStep;

public class PutUserMessageStep extends PutProcessStep {

	public PutUserMessageStep(String userId, BaseMessage userMessage, ProcessStep nextStep) {
		super(userId, H2HConstants.USER_MESSAGE_QUEUE_KEY, new NetworkContentWrapper<BaseMessage>(userMessage), nextStep);
	}
	
	@Override
	public void start() {
		// TODO encrypt here
		super.start();
	}
}
