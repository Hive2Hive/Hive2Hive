package org.hive2hive.core.process.common;

import java.util.Collection;

import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.FutureDirect;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.messages.AcceptanceReply;
import org.hive2hive.core.network.messages.BaseMessage;
import org.hive2hive.core.network.messages.SendingBehavior;
import org.hive2hive.core.network.messages.direct.response.IResponseCallBackHandler;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.network.messages.request.IRequestMessage;
import org.hive2hive.core.process.ProcessStep;

abstract public class BaseMessageProcessStep extends ProcessStep implements IResponseCallBackHandler {

	private final static Logger logger = H2HLoggerFactory.getLogger(BaseMessageProcessStep.class);

	protected final BaseMessage message;
	protected final ProcessStep nextStep;

	public BaseMessageProcessStep(BaseMessage message, ProcessStep nextStep) {
		this.message = message;
		this.nextStep = nextStep;
	}

	@Override
	public void start() {
		send(message);
	}

	@Override
	public void rollBack() {
		// nothing to rollback
	}

	private void send(BaseMessage message) {
		if (message instanceof IRequestMessage) {
			IRequestMessage requestMessage = (IRequestMessage) message;
			requestMessage.setCallBackHandler(this);
		}
		FutureDirect futureDirect = getNetworkManager().send(message);
		futureDirect.addListener(new FutureDirectListener());
	}

	protected void handleSendingSuccess() {
		getProcess().nextStep(nextStep);
	}

	/**
	 * This method is called if a failure is detected while sending this
	 * message. The idea is that sending failures are handled by the message
	 * itself, because the message is the only entity which knows how to perform
	 * from the point of a failure on. Some messages don't need do be sent
	 * again, while others need a redirect or wish to inform special (potential
	 * several) customers about the failure.</br> This abstract class implements
	 * the default behavior for {@link SendingBehavior#SEND_MAX_ALLOWED_TIMES}.
	 * Subclasses which need a different or more elaborated failure handling
	 * have to extend or override this method.
	 * 
	 * @param reply
	 *            the reply of the sending attempts
	 */
	public void handleSendingFailure(AcceptanceReply reply) {
		switch (reply) {
			case OK:
				logger.error("Trying to handle a AcceptanceReply.OK as a failure.");
				throw new IllegalArgumentException("AcceptanceReply.OK is not a failure.");
			case FAILURE:
				if (SendingBehavior.SEND_MAX_ALLOWED_TIMES == message.getSendingBehavior()) {
					if (message.getSendingCounter() < H2HConstants.MAX_MESSAGE_SENDING) {
						logger.warn(String.format("Message reply was failure. Resending #%s...",
								message.getSendingCounter()));
						FutureDirect futureDirect = getNetworkManager().send(message);
						futureDirect.addListener(new FutureDirectListener());
					} else {
						logger.error(String
								.format("Message does not getting accepted by the targets in %d tries. Details:\n source node id = '%s' target key = '%s'",
										message.getSendingCounter(), getNetworkManager().getNodeId(),
										message.getTargetKey()));
						getProcess().rollBack("Message does not getting accepted by the targets.");
					}
				} else {
					logger.warn(String
							.format("Message not accepted by the target after one try. Details:\n target key = '%s' message id = '%s'",
									message.getTargetKey(), message.getMessageID()));
					getProcess().rollBack("Message not accepted by the target after one try.");
				}
				break;
			default:
				logger.error(String.format("Unkown AcceptanceReply argument: %s", reply));
				throw new IllegalArgumentException(
						String.format("Unkown AcceptanceReply argument: %s", reply));
		}
	}

	public abstract void handleResponseMessage(ResponseMessage responseMessage);

	private class FutureDirectListener extends BaseFutureAdapter<FutureDirect> {

		@Override
		public void operationComplete(FutureDirect future) throws Exception {
			AcceptanceReply reply = extractAcceptanceReply(future);
			if (reply == AcceptanceReply.OK) {
				handleSendingSuccess();
			} else {
				handleSendingFailure(reply);
			}
		}

		private AcceptanceReply extractAcceptanceReply(FutureDirect aFuture) {
			String errorReason = "";
			if (aFuture.isSuccess()) {
				Collection<Object> returndedObject = aFuture.getRawDirectData2().values();
				if (returndedObject == null) {
					errorReason = "Returned object is null.";
				} else if (returndedObject.isEmpty()) {
					errorReason = "Returned raw data is empty.";
				} else {
					Object firstReturnedObject = returndedObject.iterator().next();
					if (firstReturnedObject == null) {
						errorReason = "First returned object is null.";
					} else if (firstReturnedObject instanceof AcceptanceReply) {
						AcceptanceReply reply = (AcceptanceReply) firstReturnedObject;
						return reply;
					} else {
						errorReason = "The returned object was not of type AcceptanceReply!";
					}
				}
				logger.error(String
						.format("A failure while sending a message occured. Info: reason = '%s' from (node ID) = '%s'",
								errorReason, getNetworkManager().getNodeId()));
				return AcceptanceReply.FAILURE;
			} else {
				logger.error(String.format("Future not successful. Reason = '%s' from (node ID) = '%s'",
						aFuture.getFailedReason(), getNetworkManager().getNodeId()));
				return AcceptanceReply.FUTURE_FAILURE;
			}
		}

	}

}
