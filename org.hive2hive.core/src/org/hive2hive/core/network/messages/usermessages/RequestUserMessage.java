package org.hive2hive.core.network.messages.usermessages;

import java.io.Serializable;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.network.messages.direct.response.IResponseCallBackHandler;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.network.messages.request.IRequestMessage;

/**
 * Abstraction of {@link UserMessage}s that request a response from the receiver.
 * 
 * @author Christian
 * 
 */
public abstract class RequestUserMessage extends UserMessage implements IRequestMessage {

	private static final long serialVersionUID = -3675295783563372557L;

	protected final PeerAddress senderAddress;
	private IResponseCallBackHandler handler;

	public RequestUserMessage(PeerAddress senderAddress, PeerAddress targetAddress) {
		super(targetAddress);

		this.senderAddress = senderAddress;
	}

	@Override
	public void setCallBackHandler(IResponseCallBackHandler handler) {
		this.handler = handler;
	}

	@Override
	public IResponseCallBackHandler getCallBackHandler() {
		return handler;
	}

	/**
	 * Configures the {@link ResponseMessage} for this {@link RequestUserMessage} with the correct message ID
	 * and receiver address.
	 * 
	 * @param content The content of the response.
	 * @return The configured {@link ResponseMessage}.
	 */
	protected ResponseMessage createResponse(Serializable content) {
		return new ResponseMessage(messageID, senderAddress, content);
	}
	
	/**
	 * Sends the {@link ResponseMessage} to its requester.
	 * @param response The {@link ResponseMessage} created with {@link RequestUserMessage#createResponse(Serializable)}.
	 */
	protected void sendResponse(ResponseMessage response) {
		networkManager.sendDirect(response);
	}
}