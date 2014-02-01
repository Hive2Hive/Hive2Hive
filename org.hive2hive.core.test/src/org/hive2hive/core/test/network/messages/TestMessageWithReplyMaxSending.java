package org.hive2hive.core.test.network.messages;

import net.tomp2p.peers.Number160;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.messages.AcceptanceReply;
import org.hive2hive.core.network.messages.direct.response.IResponseCallBackHandler;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.network.messages.request.RoutedRequestMessage;
import org.hive2hive.core.test.H2HTestData;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.junit.Assert;

/**
 * Used to test response messages and callback handlers. For further detail see
 * {@link BaseRequestMessageTest#testSendingAnAsynchronousMessageWithReply()}
 * 
 * @author Nendor, Seppi, Nico
 */
public class TestMessageWithReplyMaxSending extends RoutedRequestMessage {

	private static final long serialVersionUID = 6358613094488111567L;

	private final String contentKey;

	public TestMessageWithReplyMaxSending(String targetKey, String contentKey) {
		super(targetKey);
		this.contentKey = contentKey;
	}

	@Override
	public void run() {
		String secret = NetworkTestUtil.randomString();

		try {
			networkManager
					.getDataManager()
					.put(Number160.createHash(networkManager.getNodeId()), H2HConstants.TOMP2P_DEFAULT_KEY,
							Number160.createHash(contentKey), new H2HTestData(secret), null)
					.awaitUninterruptibly();
		} catch (NoPeerConnectionException e) {
			Assert.fail();
		}

		TestResponseMessageMaxSending responseMessage = new TestResponseMessageMaxSending(getMessageID(),
				getSenderAddress(), secret);
		Assert.assertTrue(networkManager.sendDirect(responseMessage, getSenderPublicKey()));
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

			try {
				networkManager
						.getDataManager()
						.put(Number160.createHash(networkManager.getNodeId()),
								H2HConstants.TOMP2P_DEFAULT_KEY, Number160.createHash(contentKey),
								new H2HTestData(receivedSecret), null).awaitUninterruptibly();
			} catch (NoPeerConnectionException e) {
				Assert.fail();
			}
		}

	}

}
