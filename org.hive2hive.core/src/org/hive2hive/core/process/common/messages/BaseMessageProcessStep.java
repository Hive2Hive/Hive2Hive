package org.hive2hive.core.process.common.messages;

import net.tomp2p.futures.FutureDirect;
import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.network.messages.BaseMessage;
import org.hive2hive.core.network.messages.IBaseMessageListener;
import org.hive2hive.core.network.messages.direct.response.IResponseCallBackHandler;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.network.messages.futures.FutureDirectListener;
import org.hive2hive.core.network.messages.request.BaseRequestMessage;
import org.hive2hive.core.network.messages.request.IRequestMessage;
import org.hive2hive.core.process.ProcessStep;

/**
 * This is a process step for sending a {@link BaseMessage}.
 * </br></br>
 * 
 * <b>Design decision:</b>
 * <ul>
 * <li>When a request message (e.g. {@link BaseRequestMessage}) which implements the {@link IRequestMessage}
 * interface is sent, the process step acts also as a {@link IResponseCallBackHandler} callback handler for
 * this message. This means that a </li>
 * </ul>
 * 
 * <b>Important:</b> For sending a {@link BaseDirectMessage} please use {@link BaseDirectMessageProcessStep}
 * which sends the message according a given {@link PeerAddress}.
 * 
 * @author Seppi
 */
abstract public class BaseMessageProcessStep extends ProcessStep implements IBaseMessageListener,
		IResponseCallBackHandler {

	protected final BaseMessage message;
	protected final ProcessStep nextStep;

	/**
	 * Constructor for a process step which sends a {@link BaseMessage}.
	 * 
	 * @param message
	 *            the message to send
	 * @param nextStep
	 *            the next process step
	 */
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
