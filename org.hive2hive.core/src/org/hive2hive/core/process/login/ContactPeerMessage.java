package org.hive2hive.core.process.login;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.network.messages.request.DirectRequestMessage;

public class ContactPeerMessage extends DirectRequestMessage {

	private static final long serialVersionUID = -5335863637956648142L;
	
	private String evidenceContent;

	public ContactPeerMessage(PeerAddress targetPeerAddress, String evidenceContent) {
		super(targetPeerAddress);
		
		this.evidenceContent = evidenceContent;
	}

	@Override
	public void run() {		
		// send a response with the evidentContent -> proves this peer could decrypt and read the message
		sendDirectResponse(createResponse(evidenceContent));
	}

}
