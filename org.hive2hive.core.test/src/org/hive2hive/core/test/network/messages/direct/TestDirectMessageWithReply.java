package org.hive2hive.core.test.network.messages.direct;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.messages.AcceptanceReply;
import org.hive2hive.core.network.messages.direct.response.IResponseCallBackHandler;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.network.messages.request.DirectRequestMessage;
import org.hive2hive.core.test.H2HTestData;
import org.hive2hive.core.test.network.NetworkTestUtil;

public class TestDirectMessageWithReply extends DirectRequestMessage {

	private static final long serialVersionUID = 718405880279379041L;
	
	private final String contentKey;

	public TestDirectMessageWithReply(PeerAddress targetAddress, String contentKey) {
		super(targetAddress);
		this.contentKey = contentKey;
	}

	@Override
	public void run() {
		String secret = NetworkTestUtil.randomString();

		networkManager.putLocal(networkManager.getNodeId(), contentKey, new H2HTestData(secret));

		sendDirectResponse(createResponse(secret));
	}

	@Override
	public AcceptanceReply accept() {
		return AcceptanceReply.OK;
	}

	public class TestCallBackHandler implements IResponseCallBackHandler {

		private final NetworkManager networkManager;

		public TestCallBackHandler(NetworkManager aNetworkManager) {
			networkManager = aNetworkManager;
		}

		@Override
		public void handleResponseMessage(ResponseMessage responseMessage) {
			String receivedSecret = (String) responseMessage.getContent();
			networkManager.putLocal(networkManager.getNodeId(), contentKey, new H2HTestData(receivedSecret));
		}

	}

}
