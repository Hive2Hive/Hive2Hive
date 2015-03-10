package org.hive2hive.core.network.messages.direct;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.events.framework.interfaces.IUserEventGenerator;
import org.hive2hive.core.events.implementations.UserLoginEvent;
import org.hive2hive.core.network.messages.request.DirectRequestMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple direct request message to contact and check if receiving node is alive.
 * 
 * @author Seppi, Nico, Christian
 */
public class ContactPeerMessage extends DirectRequestMessage implements IUserEventGenerator {

	private static final long serialVersionUID = 4949538351342930783L;

	private static final Logger logger = LoggerFactory.getLogger(ContactPeerMessage.class);

	private final String evidenceContent;

	public ContactPeerMessage(PeerAddress targetPeerAddress, String evidenceContent) {
		super(targetPeerAddress);
		this.evidenceContent = evidenceContent;
	}

	@Override
	public void run() {
		// generate an event with the new client
		String currentUser = networkManager.getUserId();
		UserLoginEvent event = new UserLoginEvent(currentUser, senderAddress);
		networkManager.getEventBus().publish(event);
		logger.debug("Published login event of user {} with new client {}", currentUser, senderAddress);

		logger.debug("Sending a contact peer response message. Requesting address = '{}'.", getSenderAddress());
		// send a response with the evidentContent -> proves this peer could decrypt and read the message
		sendDirectResponse(createResponse(evidenceContent));
	}

}
