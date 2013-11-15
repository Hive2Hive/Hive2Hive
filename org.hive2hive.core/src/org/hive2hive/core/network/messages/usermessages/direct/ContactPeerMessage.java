package org.hive2hive.core.network.messages.usermessages.direct;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.messages.request.DirectRequestMessage;

/**
 * A simple direct request message to contact and check if receiving node is alive.
 * 
 * @author Seppi, Nico, Christian
 */
public class ContactPeerMessage extends DirectRequestMessage {

	private static final long serialVersionUID = 4949538351342930783L;

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(ContactPeerMessage.class);

	private String evidenceContent;

	public ContactPeerMessage(PeerAddress targetPeerAddress, String evidenceContent) {
		super(targetPeerAddress);
		this.evidenceContent = evidenceContent;
	}

	@Override
	public void run() {
		logger.debug(String.format("Sending a contact peer response message. requesting address = '%s'",
				getSenderAddress()));
		// send a response with the evidentContent -> proves this peer could decrypt and read the message
		sendDirectResponse(createResponse(evidenceContent));
	}

}
