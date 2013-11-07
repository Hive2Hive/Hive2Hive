package org.hive2hive.core.process.common.messages;

import net.tomp2p.futures.FutureResponse;

import org.hive2hive.core.network.messages.IBaseMessageListener;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.network.messages.futures.FutureResponseListener;
import org.hive2hive.core.network.messages.request.IRequestMessage;
import org.hive2hive.core.process.ProcessStep;

abstract public class BaseDirectMessageProcessStep extends BaseMessageProcessStep implements
		IBaseMessageListener {

	public BaseDirectMessageProcessStep(BaseDirectMessage message, ProcessStep nextStep) {
		super(message, nextStep);
	}

	@Override
	public void start() {
		sendDirect((BaseDirectMessage) message);
	}

	protected void sendDirect(BaseDirectMessage message) {
		if (message instanceof IRequestMessage) {
			IRequestMessage requestMessage = (IRequestMessage) message;
			requestMessage.setCallBackHandler(this);
		}
		FutureResponse futureResponse = getNetworkManager().sendDirect(message);
		futureResponse.addListener(new FutureResponseListener(this, message, getNetworkManager()));
	}

	public void onSuccess() {
		if (message instanceof IRequestMessage)
			return;
		getProcess().setNextStep(nextStep);
	}

	public void onFailure() {
		if (message instanceof IRequestMessage)
			return;
		getProcess().stop("Sending direct message failed.");
	}

}
