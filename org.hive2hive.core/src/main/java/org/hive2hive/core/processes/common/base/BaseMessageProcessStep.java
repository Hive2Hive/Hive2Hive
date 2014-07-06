package org.hive2hive.core.processes.common.base;

import java.security.PublicKey;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.exceptions.SendFailedException;
import org.hive2hive.core.network.messages.BaseMessage;
import org.hive2hive.core.network.messages.IMessageManager;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.network.messages.direct.response.IResponseCallBackHandler;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.network.messages.request.IRequestMessage;
import org.hive2hive.core.network.messages.request.RoutedRequestMessage;
import org.hive2hive.processframework.abstracts.ProcessStep;

/**
 * This is a process step for sending a {@link BaseMessage}.
 * </br></br>
 * 
 * <b>Important:</b> For sending a {@link BaseDirectMessage} please use {@link BaseDirectMessageProcessStep}
 * which sends the message according a given {@link PeerAddress}.</br></br>
 * 
 * <b>Design decision:</b>
 * <ul>
 * <li>When a request message (e.g. {@link RoutedRequestMessage}) which implements the {@link IRequestMessage}
 * interface is sent, the process step acts also as a {@link IResponseCallBackHandler} callback handler for
 * this message. The whole callback functionality has to be (if desired) implemented in the
 * {@link BaseMessageProcessStep#handleResponseMessage(ResponseMessage)} method.</li>
 * <li>All messages in <code>Hive2Hive</code> are sent synchronous</li>
 * </ul>
 * 
 * @author Seppi, Nico
 */
public abstract class BaseMessageProcessStep extends ProcessStep implements IResponseCallBackHandler {

	protected final IMessageManager messageManager;

	public BaseMessageProcessStep(IMessageManager messageManager) {
		this.messageManager = messageManager;
	}

	protected void send(BaseMessage message, PublicKey receiverPublicKey) throws SendFailedException {
		if (message instanceof IRequestMessage) {
			IRequestMessage requestMessage = (IRequestMessage) message;
			requestMessage.setCallBackHandler(this);
		}
		boolean success = messageManager.send(message, receiverPublicKey);
		if (!success) {
			throw new SendFailedException();
		}
	}

	public abstract void handleResponseMessage(ResponseMessage responseMessage);

}
