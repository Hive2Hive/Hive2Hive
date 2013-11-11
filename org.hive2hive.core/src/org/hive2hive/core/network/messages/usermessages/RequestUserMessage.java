package org.hive2hive.core.network.messages.usermessages;

import java.io.Serializable;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.network.messages.direct.response.IResponseCallBackHandler;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.network.messages.request.IRequestMessage;

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
	
	protected ResponseMessage createResponse(Serializable content){
		return new ResponseMessage(messageID, senderAddress, content);
	}
}