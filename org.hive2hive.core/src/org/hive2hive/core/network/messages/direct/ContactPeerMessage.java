package org.hive2hive.core.network.messages.direct;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.network.messages.request.DirectRequestMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple direct request message to contact and check if receiving node is alive.
 * 
 * @author Seppi, Nico, Christian
 */
public class ContactPeerMessage extends DirectRequestMessage {

	private static final long serialVersionUID = 4949538351342930783L;

	private static final Logger logger = LoggerFactory.getLogger(ContactPeerMessage.class);

	private String evidenceContent;

	public ContactPeerMessage(PeerAddress targetPeerAddress, String evidenceContent) {
		super(targetPeerAddress);
		this.evidenceContent = evidenceContent;
	}

	@Override
	public void run() {
		logger.debug("Sending a contact peer response message. Requesting address = '{}'.",
				getSenderAddress());
		// send a response with the evidentContent -> proves this peer could decrypt and read the message
		sendDirectResponse(createResponse(evidenceContent));
	}

}
