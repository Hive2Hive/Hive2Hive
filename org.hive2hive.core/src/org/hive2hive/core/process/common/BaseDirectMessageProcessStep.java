package org.hive2hive.core.process.common;

import java.util.List;

import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.FutureResponse;
import net.tomp2p.message.Buffer;

import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.messages.AcceptanceReply;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.network.messages.request.IRequestMessage;
import org.hive2hive.core.process.ProcessStep;

abstract public class BaseDirectMessageProcessStep extends BaseMessageProcessStep {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(BaseDirectMessageProcessStep.class);

	public BaseDirectMessageProcessStep(BaseDirectMessage message, ProcessStep nextStep) {
		super(message, nextStep);
	}

	@Override
	public void start() {
		sendDirect((BaseDirectMessage) message);
	}

	private void sendDirect(BaseDirectMessage message) {
		if (message instanceof IRequestMessage) {
			IRequestMessage requestMessage = (IRequestMessage) message;
			requestMessage.setCallBackHandler(this);
		}
		FutureResponse futureResponse = getNetworkManager().sendDirect(message);
		futureResponse.addListener(new FutureResponseListener());
	}

	@Override
	public void handleSendingFailure(AcceptanceReply reply) {
		logger.debug(String.format("Have to handle a sending failure. AcceptanceReply='%s'", reply));
		BaseDirectMessage directMessage = (BaseDirectMessage) message;
		if (AcceptanceReply.FUTURE_FAILURE == reply) {
			logger.debug(String.format(
					"Failure while sending this message directly using the peer address '%s' ",
					directMessage.getTargetAddress()));
			if (directMessage.needsRedirectedSend()) {
				// TODO fallback on routed sending
			}
		} else {
			super.handleSendingFailure(reply);
		}
	}

	private class FutureResponseListener extends BaseFutureAdapter<FutureResponse> {

		@Override
		public void operationComplete(FutureResponse future) throws Exception {
			AcceptanceReply reply = extractAcceptanceReply(future);
			if (reply == AcceptanceReply.OK) {
				handleSendingSuccess();
			} else {
				handleSendingFailure(reply);
			}
		}

		private AcceptanceReply extractAcceptanceReply(FutureResponse aFuture) {
			String errorReason = "";
			if (aFuture.isSuccess()) {
				List<Buffer> returnedBuffer = aFuture.getResponse().getBufferList();
				if (returnedBuffer == null) {
					errorReason = "Returned buffer is null.";
				} else if (returnedBuffer.isEmpty()) {
					errorReason = "Returned buffer is empty.";
				} else {
					Buffer firstReturnedBuffer = returnedBuffer.iterator().next();
					if (firstReturnedBuffer == null) {
						errorReason = "First returned buffer is null.";
					} else {
						Object responseObject;
						try {
							responseObject = firstReturnedBuffer.object();
							if (responseObject instanceof AcceptanceReply) {
								AcceptanceReply reply = (AcceptanceReply) responseObject;
								return reply;
							} else {
								errorReason = "The returned object was not of type AcceptanceReply!";
							}
						} catch (Exception e) {
							errorReason = "Exception occured while getting the object.";
						}
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
