package org.hive2hive.core.test.process.common.massages;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.messages.AcceptanceReply;
import org.hive2hive.core.network.messages.direct.response.IResponseCallBackHandler;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.network.messages.request.DirectRequestMessage;
import org.hive2hive.core.test.H2HTestData;
import org.hive2hive.core.test.network.NetworkTestUtil;

/**
 * A test message which is direct and is a request. Used to test response messages and callback handlers. For
 * further detail see
 * {@link BaseDirectMessageProcessStepTest#baseDirectMessageProcessStepTestWithARequestMessage()}
 * 
 * @author Seppi
 */
public class TestDirectMessageWithReply extends DirectRequestMessage {

	private static final long serialVersionUID = 6358613094488111567L;

	private final String contentKey;

	public TestDirectMessageWithReply(PeerAddress targetAddress, String contentKey) {
		super(targetAddress);
		this.contentKey = contentKey;
	}

	@Override
	public void run() {
		String secret = NetworkTestUtil.randomString();

		networkManager.getDataManager().putLocal(networkManager.getNodeId(), contentKey, new H2HTestData(secret));

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
			networkManager.getDataManager().putLocal(networkManager.getNodeId(), contentKey, new H2HTestData(receivedSecret));
		}

	}
	
	@Override
	public boolean checkSignature(byte[] data, byte[] signature, String userId) {
		if (!networkManager.getUserId().equals(userId)) {
			return false;
		} else {
			return verify(data, signature, networkManager.getPublicKey());
		}
	}

}
