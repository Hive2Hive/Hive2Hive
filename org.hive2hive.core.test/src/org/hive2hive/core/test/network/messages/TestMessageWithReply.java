package org.hive2hive.core.test.network.messages;

import net.tomp2p.peers.Number160;

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
 * @author Seppi
 */
public class TestMessageWithReply extends RoutedRequestMessage {

	private static final long serialVersionUID = 6358613094488111567L;

	private final String contentKey;

	public TestMessageWithReply(String targetKey, String contentKey) {
		super(targetKey);
		this.contentKey = contentKey;
	}

	@Override
	public void run() {
		String secret = NetworkTestUtil.randomString();

		Number160 lKey = Number160.createHash(networkManager.getNodeId());
		Number160 cKey = Number160.createHash(contentKey);
		try {
			networkManager.getDataManager().put(lKey, Number160.ZERO, cKey, new H2HTestData(secret), null)
					.awaitUninterruptibly();
		} catch (NoPeerConnectionException e) {
			Assert.fail();
		}

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
			Number160 lKey = Number160.createHash(networkManager.getNodeId());
			Number160 cKey = Number160.createHash(contentKey);
			try {
				networkManager.getDataManager()
						.put(lKey, Number160.ZERO, cKey, new H2HTestData(receivedSecret), null)
						.awaitUninterruptibly();
			} catch (NoPeerConnectionException e) {
				Assert.fail();
			}
		}

	}

}
