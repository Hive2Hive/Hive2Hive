package org.hive2hive.core.network.messages.request;

import java.io.Serializable;

import org.hive2hive.core.network.messages.BaseMessage;
import org.hive2hive.core.network.messages.direct.response.IResponseCallBackHandler;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;

/**
 * Abstract message type of a <b>routed</b> message that requests a response.
 * 
 * @author Christian
 * 
 */
public abstract class RoutedRequestMessage extends BaseMessage implements IRequestMessage {

	private static final long serialVersionUID = 4510609215735076075L;

	private IResponseCallBackHandler handler;

	public RoutedRequestMessage(String targetKey) {
		super(targetKey);
	}

	public final IResponseCallBackHandler getCallBackHandler() {
		return handler;
	}

	public final void setCallBackHandler(IResponseCallBackHandler handler) {
		this.handler = handler;
	}

	public final ResponseMessage createResponse(Serializable content) {
		return new ResponseMessage(messageID, senderAddress, content);
	}

	public void sendDirectResponse(ResponseMessage response) {
		messageManager.sendDirect(response, getSenderPublicKey());
	}
}