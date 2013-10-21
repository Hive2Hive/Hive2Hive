package org.hive2hive.core.network.messages.request.callback;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;

/**
 * The callback handler receives a response on a peer address discovery request
 * message. Then the handler resends the given direct message with the received
 * peer address.
 * 
 * @author Seppi
 */
public class PeerAddressDiscoveryDirectMessagingCallBackHandler implements
		ICallBackHandler {

	private final NetworkManager networkManager;
	private final BaseDirectMessage message;

	public PeerAddressDiscoveryDirectMessagingCallBackHandler(
			NetworkManager networkManager, BaseDirectMessage message) {
		this.networkManager = networkManager;
		this.message = message;
	}

	@Override
	public void handleReturnMessage(ResponseMessage asyncReturnMessage) {
		PeerAddress peerAddress = (PeerAddress) asyncReturnMessage.getContent();
		message.setTargetPeerAddress(peerAddress);
		networkManager.send(message);
	}

}
