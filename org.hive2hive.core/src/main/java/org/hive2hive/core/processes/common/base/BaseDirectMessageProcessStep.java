package org.hive2hive.core.processes.common.base;

import java.security.PublicKey;

import org.hive2hive.core.exceptions.SendFailedException;
import org.hive2hive.core.network.messages.BaseMessage;
import org.hive2hive.core.network.messages.IMessageManager;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.network.messages.direct.response.IResponseCallBackHandler;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.network.messages.request.IRequestMessage;

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
 * <li>All messages in <code>Hive2Hive</code> are sent synchronous</li>
 * </ul>
 * 
 * @author Seppi, Nico
 */
public abstract class BaseDirectMessageProcessStep extends BaseMessageProcessStep {

	public BaseDirectMessageProcessStep(IMessageManager messageManager) {
		super(messageManager);
	}

	protected void sendDirect(BaseDirectMessage message, PublicKey receiverPublicKey) throws SendFailedException {
		if (message instanceof IRequestMessage) {
			IRequestMessage requestMessage = (IRequestMessage) message;
			requestMessage.setCallBackHandler(this);
		}

		boolean success = messageManager.sendDirect(message, receiverPublicKey);
		if (!success) {
			throw new SendFailedException();
		}
	}

	@Override
	protected void send(BaseMessage message, PublicKey receiverPublicKey) throws SendFailedException {
		throw new UnsupportedOperationException("Use 'sendDirect' when inheriting from this class");
	}
}
