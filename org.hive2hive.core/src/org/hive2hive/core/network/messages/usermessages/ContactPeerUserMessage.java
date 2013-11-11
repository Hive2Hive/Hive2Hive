package org.hive2hive.core.network.messages.usermessages;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.network.messages.direct.response.ResponseMessage;

public class ContactPeerUserMessage extends RequestUserMessage {

	private static final long serialVersionUID = -5335863637956648142L;
	
	private final String evidenceContent;

	public ContactPeerUserMessage(PeerAddress senderAddress, PeerAddress targetAddress, String evidenceContent) {
		super(senderAddress, targetAddress);
		
		this.evidenceContent = evidenceContent;
	}

	@Override
	public void run() {
		
		// send a response with the evidentContent -> proves this peer could decrypt and read the message
		ResponseMessage response = createResponse(evidenceContent);
		networkManager.sendDirect(response);
	}
}
