package org.hive2hive.core.process.register;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.UserMessageQueue;
import org.hive2hive.core.process.common.put.BasePutProcessStep;

public class PutUserMessageQueue extends BasePutProcessStep {

	private final UserMessageQueue queue;

	public PutUserMessageQueue(UserMessageQueue queue) {
		super(null);
		this.queue = queue;
	}

	@Override
	public void start() {
		put(queue.getUserId(), H2HConstants.USER_MESSAGE_QUEUE_KEY, queue);
	}

}
