package org.hive2hive.core.network.messages;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.network.messages.request.BaseRequestMessage;

/**
 * This message returns the peer address of the receiver to the sender.
 * 
 * @author andri
 * 
 */
public class PeerAddressDiscoveryMessage extends BaseRequestMessage {

	private static final H2HLogger logger = H2HLoggerFactory
			.getLogger(PeerAddressDiscoveryMessage.class);

	private static final long serialVersionUID = 7779522462719187961L;

	public PeerAddressDiscoveryMessage(String aTargetKey,
			PeerAddress aSenderAddress) {
		super(aTargetKey, aSenderAddress);
	}

	@Override
	public void run() {
		logger.debug(String
				.format("Handling a proxy address discovery message"));

		ResponseMessage returnMessage = new ResponseMessage(getMessageID(),
				getTargetKey(), getSenderAddress(),
				networkManager.getPeerAddress());
		networkManager.send(returnMessage);
	}

	@Override
	public AcceptanceReply accept() {
		return AcceptanceReply.OK;
	}

}
