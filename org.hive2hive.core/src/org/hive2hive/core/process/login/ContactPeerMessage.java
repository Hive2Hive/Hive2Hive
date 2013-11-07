package org.hive2hive.core.process.login;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.network.messages.direct.response.IResponseCallBackHandler;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.network.messages.request.IRequestMessage;

public class ContactPeerMessage extends BaseDirectMessage implements IRequestMessage {

	private static final long serialVersionUID = -5335863637956648142L;
	
	private IResponseCallBackHandler handler;
	private String evidenceContent;

	public ContactPeerMessage(PeerAddress targetPeerAddress, String evidenceContent) {
		super(targetPeerAddress);
		
		this.evidenceContent = evidenceContent;
	}

	@Override
	public void run() {
		
		// send a response with the evidentContent -> proves this peer could decrypt and read the message
		ResponseMessage response = new ResponseMessage(messageID, targetPeerAddress, evidenceContent);
		networkManager.sendDirect(response);
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
