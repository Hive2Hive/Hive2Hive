package org.hive2hive.core.process.common;

import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.FutureResponse;

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

	private BaseDirectMessage getDirectMessage() {
		return (BaseDirectMessage) message;
	}

	@Override
	public void start() {
		sendDirect(getDirectMessage());
	}

	protected void sendDirect(BaseDirectMessage message) {
		if (message instanceof IRequestMessage) {
			IRequestMessage requestMessage = (IRequestMessage) message;
			requestMessage.setCallBackHandler(this);
		}
		FutureResponse futureResponse = getNetworkManager().sendDirect(message);
		futureResponse.addListener(new FutureResponseListener());
	}

	private void handleDirectSendingSuccess() {
		getProcess().nextStep(nextStep);
	}

	public void handleDirectSendingFailure(AcceptanceReply reply) {
		boolean directResending = getDirectMessage().handleSendingFailure(reply);
		if (directResending) {
			sendDirect(getDirectMessage());
		} else {
			if (getDirectMessage().needsRedirectedSend()) {
				logger.warn(String
						.format("Sending direct message failed. Using normal routed sending as fallback. target key = '&s' target address = '%s'",
								getDirectMessage().getTargetKey(), getDirectMessage().getTargetAddress()));
				send(message);
			} else {
				getProcess().rollBack("Sending direct message failed.");
			}
		}
	}

	private class FutureResponseListener extends BaseFutureAdapter<FutureResponse> {

		@Override
		public void operationComplete(FutureResponse future) throws Exception {
			AcceptanceReply reply = getDirectMessage().extractAcceptanceReply(future);
			if (reply == AcceptanceReply.OK) {
				handleDirectSendingSuccess();
			} else {
				handleDirectSendingFailure(reply);
			}
		}

	}
}
