package org.hive2hive.core.process.common;

import java.util.Collection;

import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.FutureDirect;

import org.apache.log4j.Logger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.messages.AcceptanceReply;
import org.hive2hive.core.network.messages.BaseMessage;
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

	protected void handleSendingFailure(AcceptanceReply reply){
		boolean resending = message.handleSendingFailure(reply);
		if (resending){
			FutureDirect futureDirect = getNetworkManager().send(message);
			futureDirect.addListener(new FutureDirectListener());
		} else {
			getProcess().rollBack("Sending message failed.");
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
