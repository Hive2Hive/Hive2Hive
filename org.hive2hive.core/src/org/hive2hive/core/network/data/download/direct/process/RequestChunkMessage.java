package org.hive2hive.core.network.data.download.direct.process;

import java.security.PublicKey;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.network.messages.direct.ContactPeerMessage;
import org.hive2hive.core.network.messages.request.DirectRequestMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestChunkMessage extends DirectRequestMessage {

	private static final long serialVersionUID = 3591235525796608138L;
	private static final Logger logger = LoggerFactory.getLogger(ContactPeerMessage.class);

	private final PublicKey fileKey;

	public RequestChunkMessage(PeerAddress targetPeerAddress, PublicKey fileKey) {
		super(targetPeerAddress);
		this.fileKey = fileKey;
	}

	@Override
	public void run() {
		logger.debug("Received request for a chunk from peer {}", senderAddress);

		// search user profile for this file
		H2HSession session;
		try {
			session = networkManager.getSession();
		} catch (NoSessionException e) {
			logger.error("Cannot answer because session is invalid");
			// TODO: send response saying no
		}

		// TODO
		// find file in user profile
		// check if file is on disk
		// retrieve the requested file part (offset and length)
		// return the content of the file part
	}

}
