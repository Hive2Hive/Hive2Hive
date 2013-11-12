package org.hive2hive.core.network.messages.usermessages.routed;

import java.io.Serializable;

import org.hive2hive.core.network.messages.BaseMessage;

public final class NextFromQueueResponse implements Serializable {

	private static final long serialVersionUID = -2850130135183154805L;

	private final BaseMessage userMessage;
	private final int remainingCount;

	public NextFromQueueResponse(BaseMessage message, int remaining) {
		this.userMessage = message;
		this.remainingCount = remaining;
	}

	public BaseMessage getUserMessage() {
		return userMessage;
	}

	public int getRemainingCount() {
		return remainingCount;
	}
}
