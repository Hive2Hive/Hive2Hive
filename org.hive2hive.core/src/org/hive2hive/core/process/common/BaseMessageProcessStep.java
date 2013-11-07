package org.hive2hive.core.process.common;

import net.tomp2p.futures.FutureDirect;

import org.hive2hive.core.network.messages.BaseMessage;
import org.hive2hive.core.network.messages.FutureDirectListener;
import org.hive2hive.core.network.messages.IBaseMessageListener;
import org.hive2hive.core.network.messages.direct.response.IResponseCallBackHandler;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.network.messages.request.IRequestMessage;
import org.hive2hive.core.process.ProcessStep;

abstract public class BaseMessageProcessStep extends ProcessStep implements IBaseMessageListener,
		IResponseCallBackHandler {

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
		futureDirect.addListener(new FutureDirectListener(this, message, getNetworkManager()));
	}

	public void onSuccess() {
		if (message instanceof IRequestMessage)
			return;
		getProcess().setNextStep(nextStep);
	}

	public void onFailure() {
		if (message instanceof IRequestMessage)
			return;
		getProcess().stop("Sending message failed.");
	}

	public abstract void handleResponseMessage(ResponseMessage responseMessage);

}
