package org.hive2hive.core.network.messages.usermessages.routed;

import java.io.Serializable;
import java.util.Queue;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.UserMessageQueue;
import org.hive2hive.core.network.messages.AcceptanceReply;
import org.hive2hive.core.network.messages.BaseMessage;
import org.hive2hive.core.network.messages.request.RoutedRequestMessage;

// TODO this might actually not be a user message
public class GetNextUserMessageMessage extends RoutedRequestMessage {

	private static final long serialVersionUID = 580669795666539208L;

	public GetNextUserMessageMessage(String targetKey) {
		super(targetKey);
	}

	@Override
	public synchronized void run() {

		// load the next user message
		UserMessageQueue umQueue = (UserMessageQueue) networkManager.getLocal(targetKey,
				H2HConstants.USER_MESSAGE_QUEUE_KEY);
		// TODO null handling
		Queue<BaseMessage> messageQueue = umQueue.getMessageQueue();

		// response object
		// TODO don't delete UM from queue, since it could fail to get handled
		NextFromQueueResponse responseContent = new NextFromQueueResponse(messageQueue.poll(),
				messageQueue.size());

		// send it back in a ResponseMessage
		sendDirectResponse(createResponse(responseContent));
	}

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

	@Override
	public AcceptanceReply accept() {
		// TODO Auto-generated method stub
		return null;
	}
}
