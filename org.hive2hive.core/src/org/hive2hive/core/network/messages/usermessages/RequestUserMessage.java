package org.hive2hive.core.network.messages.usermessages;

import org.hive2hive.core.network.messages.direct.response.IResponseCallBackHandler;
import org.hive2hive.core.network.messages.request.IRequestMessage;

import net.tomp2p.peers.PeerAddress;

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
}