package org.hive2hive.core.network.messages.usermessages.direct;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.network.messages.request.DirectRequestMessage;

public class ContactPeerUserMessage extends DirectRequestMessage {

	private static final long serialVersionUID = -5335863637956648142L;

	private final String evidenceContent;

	public ContactPeerUserMessage(PeerAddress targetAddress, String evidenceContent) {
		super(targetAddress);

		this.evidenceContent = evidenceContent;
	}

	@Override
	public void run() {
		// send a response with the evidentContent -> proves this peer could decrypt and read the message
		sendDirectResponse(createResponse(evidenceContent));
	}

}
