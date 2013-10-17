package org.hive2hive.core.network.messages.request.callback;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;

public class PeerAddressDiscoveryDirectMessagingCallBackHandler implements ICallBackHandler {

	private final NetworkManager networkManager;
	private final BaseDirectMessage message;

	public PeerAddressDiscoveryDirectMessagingCallBackHandler(NetworkManager aNetworkManager,
			BaseDirectMessage aMessage) {
		networkManager = aNetworkManager;
		message = aMessage;
	}

	@Override
	public void handleReturnMessage(ResponseMessage asyncReturnMessage) {
		PeerAddress peerAddress = (PeerAddress) asyncReturnMessage.getContent();
		message.setTargetPeerAddress(peerAddress);
		networkManager.send(message);
	}

}
