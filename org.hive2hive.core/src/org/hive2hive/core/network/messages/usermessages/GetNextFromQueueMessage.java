package org.hive2hive.core.network.messages.usermessages;

import java.io.Serializable;
import java.util.Queue;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.UserMessageQueue;

// TODO this might actually not be a user message
public class GetNextFromQueueMessage extends RequestUserMessage {

	private static final long serialVersionUID = 580669795666539208L;

	private final String locationKey;

	public GetNextFromQueueMessage(PeerAddress senderAddress, PeerAddress targetAddress, String locationKey) {
		super(senderAddress, targetAddress);
		this.locationKey = locationKey;
	}

	@Override
	public synchronized void run() {

		// load the next user message
		UserMessageQueue umQueue = (UserMessageQueue) networkManager.getLocal(locationKey,
				H2HConstants.USER_MESSAGE_QUEUE_KEY);
		Queue<UserMessage> messageQueue = umQueue.getMessageQueue();

		// response object
		NextFromQueueResponse responseContent = new NextFromQueueResponse(messageQueue.poll(),
				messageQueue.size());

		// send it back in a ResponseMessage
		sendResponse(createResponse(responseContent));
	}

	public final class NextFromQueueResponse implements Serializable {

		private static final long serialVersionUID = -2850130135183154805L;

		private final UserMessage userMessage;
		private final int remainingCount;

		public NextFromQueueResponse(UserMessage message, int remaining) {
			this.userMessage = message;
			this.remainingCount = remaining;
		}

		public UserMessage getUserMessage() {
			return userMessage;
		}

		public int getRemainingCount() {
			return remainingCount;
		}
	}
}
