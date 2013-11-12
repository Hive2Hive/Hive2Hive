package org.hive2hive.core.process.common.messages;

import net.tomp2p.futures.FutureResponse;

import org.hive2hive.core.network.messages.BaseMessage;
import org.hive2hive.core.network.messages.IBaseMessageListener;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.network.messages.direct.response.IResponseCallBackHandler;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.network.messages.futures.FutureResponseListener;
import org.hive2hive.core.network.messages.request.IRequestMessage;
import org.hive2hive.core.process.ProcessStep;

/**
 * This is a process step for sending a {@link BaseDirectMessage}.
 * </br></br>
 * 
 * <b>Important:</b> For sending a {@link BaseMessage} please use {@link BaseMessageProcessStep} which routes
 * the message according a given {@link BaseMessage#getTargetKey()} target key to the responsible node of that
 * key.</br></br>
 * 
 * <b>Design decision:</b>
 * <ul>
 * <li>When a direct request message implementing the {@link IRequestMessage} interface is sent, the process
 * step acts also as a {@link IResponseCallBackHandler} callback handler for this message. The whole callback
 * functionality has to be (if desired) implemented in the
 * {@link BaseDirectMessageProcessStep#handleResponseMessage(ResponseMessage)} method.</li>
 * <li>Because all message in <code>Hive2Hive</code> are sent asynchronously the message process step uses a
 * {@link FutureResponseListener} adapter to gets notified about success or failure of sending the message.
 * For that the process step implements the {@link IBaseMessageListener} interface.</li>
 * </ul>
 * 
 * @author Seppi
 */
abstract public class BaseDirectMessageProcessStep extends BaseMessageProcessStep {

	public BaseDirectMessageProcessStep(BaseDirectMessage message, ProcessStep nextStep) {
		super(message, nextStep);
	}

	@Override
	public void start() {
		// casting necessary because message in parent is a BaseMessage
		sendDirect((BaseDirectMessage) message);
	}

	protected void sendDirect(BaseDirectMessage message) {
		if (message instanceof IRequestMessage) {
			IRequestMessage requestMessage = (IRequestMessage) message;
			requestMessage.setCallBackHandler(this);
		}
		FutureResponse futureResponse = getNetworkManager().sendDirect(message);
		// TODO Add public keys
		futureResponse.addListener(new FutureResponseListener(this, message, null, getNetworkManager()));
	}

	@Override
	public void onSuccess() {
		if (message instanceof IRequestMessage)
			return;
		getProcess().setNextStep(nextStep);
	}

	@Override
	public void onFailure() {
		if (message instanceof IRequestMessage)
			return;
		getProcess().stop("Sending direct message failed.");
	}

}
