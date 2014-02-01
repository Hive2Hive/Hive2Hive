package org.hive2hive.core.network.messages.request;

import java.io.Serializable;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.network.messages.direct.response.IResponseCallBackHandler;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;

/**
 * Abstract message type of a <b>direct</b> message that requests a response.
 * 
 * @author Christian
 * 
 */
public abstract class DirectRequestMessage extends BaseDirectMessage implements IRequestMessage {

	private static final long serialVersionUID = -8363641962924723518L;

	private IResponseCallBackHandler handler;

	public DirectRequestMessage(PeerAddress targetPeerAddress) {
		super(targetPeerAddress);
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
