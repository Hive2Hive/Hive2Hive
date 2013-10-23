package org.hive2hive.core.test.network.messaging;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.messages.AcceptanceReply;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.network.messages.request.BaseRequestMessage;
import org.hive2hive.core.network.messages.request.callback.ICallBackHandler;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.network.data.TestDataWrapper;

/**
 * Used to test response messages and callback handlers. For further detail see {@link
 * BaseRequestMessageTest#testSendingAnAsynchronousMessageWithReply()}
 * 
 * @author Seppi
 */
public class TestMessageWithReply extends BaseRequestMessage {

	private static final long serialVersionUID = 6358613094488111567L;

	private final String contentKey;

	public TestMessageWithReply(String aTargetKey, PeerAddress aSenderAddress, String contentKey) {
		super(aTargetKey, aSenderAddress);
		this.contentKey = contentKey;
	}

	@Override
	public void run() {
		String secret = NetworkTestUtil.randomString();

		networkManager.putLocal(networkManager.getNodeId(), contentKey, new TestDataWrapper(secret));

		ResponseMessage responseMessage = new ResponseMessage(getMessageID(), getTargetKey(),
				getSenderAddress(), secret);
		networkManager.send(responseMessage);
	}

	@Override
	public AcceptanceReply accept() {
		return AcceptanceReply.OK;
	}

	public class TestCallBackHandler implements ICallBackHandler {

		private final NetworkManager networkManager;

		public TestCallBackHandler(NetworkManager aNetworkManager) {
			networkManager = aNetworkManager;
		}

		@Override
		public void handleReturnMessage(ResponseMessage asyncReturnMessage) {
			String receivedSecret = (String) asyncReturnMessage.getContent();
			networkManager.putLocal(networkManager.getNodeId(), contentKey, new TestDataWrapper(
					receivedSecret));
		}

	}

}
