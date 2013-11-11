package org.hive2hive.core.network.messages.usermessages;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.network.messages.direct.response.ResponseMessage;

public class GetNextFromQueueMessage extends RequestUserMessage {

	private static final long serialVersionUID = 580669795666539208L;

	public GetNextFromQueueMessage(PeerAddress senderAddress, PeerAddress targetAddress) {
		super(senderAddress, targetAddress);
	}

	@Override
	public void run() {

		// load the next user message
		
		
		// send it back in a ResponseMessage
		ResponseMessage nextUmResponse = createResponse(null);
	}
}
