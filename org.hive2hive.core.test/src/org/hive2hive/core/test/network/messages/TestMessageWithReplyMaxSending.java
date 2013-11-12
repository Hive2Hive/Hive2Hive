package org.hive2hive.core.test.network.messages;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.messages.AcceptanceReply;
import org.hive2hive.core.network.messages.IBaseMessageListener;
import org.hive2hive.core.network.messages.direct.response.IResponseCallBackHandler;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.network.messages.futures.FutureDirectListener;
import org.hive2hive.core.network.messages.request.RoutedRequestMessage;
import org.hive2hive.core.test.H2HTestData;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.junit.Assert;

/**
 * Used to test response messages and callback handlers. For further detail see
 * {@link BaseRequestMessageTest#testSendingAnAsynchronousMessageWithReply()}
 * 
 * @author Nendor, Seppi
 */
public class TestMessageWithReplyMaxSending extends RoutedRequestMessage {

	private static final long serialVersionUID = 6358613094488111567L;

	private final String contentKey;

	public TestMessageWithReplyMaxSending(String targetKey, PeerAddress senderAddress, String contentKey) {
		super(targetKey);
		this.contentKey = contentKey;
	}

	@Override
	public void run() {
		String secret = NetworkTestUtil.randomString();

		networkManager.putLocal(networkManager.getNodeId(), contentKey, new H2HTestData(secret));

		TestResponseMessageMaxSending responseMessage = new TestResponseMessageMaxSending(getMessageID(),
				getSenderAddress(), secret);
		networkManager.sendDirect(responseMessage).addListener(
				new FutureDirectListener(new IBaseMessageListener() {
					@Override
					public void onSuccess() {
					}

					@Override
					public void onFailure() {
						// should not happen
						Assert.fail("Should not failed.");
					}
				}, responseMessage, networkManager));
	}

	@Override
	public AcceptanceReply accept() {
		return AcceptanceReply.OK;
	}

	public class TestCallBackHandlerMaxSendig implements IResponseCallBackHandler {

		private final NetworkManager networkManager;

		public TestCallBackHandlerMaxSendig(NetworkManager aNetworkManager) {
			networkManager = aNetworkManager;
		}

		@Override
		public void handleResponseMessage(ResponseMessage responseMessage) {
			String receivedSecret = (String) responseMessage.getContent();
			networkManager.putLocal(networkManager.getNodeId(), contentKey, new H2HTestData(receivedSecret));
		}

	}

}
