package org.hive2hive.core.test.network.messaging;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.messages.AcceptanceReply;
import org.hive2hive.core.network.messages.FutureResponseListener;
import org.hive2hive.core.network.messages.IBaseMessageListener;
import org.hive2hive.core.network.messages.direct.response.IResponseCallBackHandler;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.network.messages.request.BaseRequestMessage;
import org.hive2hive.core.test.H2HTestData;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.junit.Assert;

/**
 * Used to test response messages and callback handlers. For further detail see {@link
 * BaseRequestMessageTest#testSendingAnAsynchronousMessageWithReply()}
 * 
 * @author Seppi
 */
public class TestMessageWithReplyMaxSending extends BaseRequestMessage {

	private static final long serialVersionUID = 6358613094488111567L;

	private final String contentKey;

	public TestMessageWithReplyMaxSending(String aTargetKey, PeerAddress aSenderAddress, String contentKey) {
		super(aTargetKey, aSenderAddress);
		this.contentKey = contentKey;
	}

	@Override
	public void run() {
		String secret = NetworkTestUtil.randomString();

		networkManager.putLocal(networkManager.getNodeId(), contentKey, new H2HTestData(secret));

		TestResponseMessageMaxSending responseMessage = new TestResponseMessageMaxSending(getMessageID(), getTargetKey(),
				networkManager.getPeerAddress(), getSenderAddress(), secret);
		networkManager.sendDirect(responseMessage).addListener(new FutureResponseListener(new IBaseMessageListener() {
			@Override
			public void onSuccess() {
			}

			@Override
			public void onFailure() {
				// should not happen
				Assert.fail("Should not failed.");
			}
		}, responseMessage, networkManager));;
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
			networkManager.putLocal(networkManager.getNodeId(), contentKey, new H2HTestData(
					receivedSecret));
		}

	}

}
