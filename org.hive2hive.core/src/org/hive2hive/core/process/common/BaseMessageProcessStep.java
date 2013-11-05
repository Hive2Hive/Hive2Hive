package org.hive2hive.core.process.common;

import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.FutureDirect;

import org.hive2hive.core.network.messages.AcceptanceReply;
import org.hive2hive.core.network.messages.BaseMessage;
import org.hive2hive.core.network.messages.direct.response.IResponseCallBackHandler;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.network.messages.request.IRequestMessage;
import org.hive2hive.core.process.ProcessStep;

abstract public class BaseMessageProcessStep extends ProcessStep implements IResponseCallBackHandler {

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

	protected void send(BaseMessage message) {
		if (message instanceof IRequestMessage) {
			IRequestMessage requestMessage = (IRequestMessage) message;
			requestMessage.setCallBackHandler(this);
		}
		FutureDirect futureDirect = getNetworkManager().send(message);
		futureDirect.addListener(new FutureDirectListener());
	}

	private void handleSendingSuccess() {
		getProcess().nextStep(nextStep);
	}

	private void handleSendingFailure(AcceptanceReply reply){
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
			AcceptanceReply reply = message.extractAcceptanceReply(future);
			if (reply == AcceptanceReply.OK) {
				handleSendingSuccess();
			} else {
				handleSendingFailure(reply);
			}
		}
		
	}

}
