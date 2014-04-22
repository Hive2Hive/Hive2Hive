package org.hive2hive.core.network.messages.direct;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.H2HTestData;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.NetworkTestUtil;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.network.messages.AcceptanceReply;
import org.hive2hive.core.network.messages.TestResponseMessageMaxSending;
import org.hive2hive.core.network.messages.direct.response.IResponseCallBackHandler;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.network.messages.request.DirectRequestMessage;
import org.junit.Assert;

public class TestDirectMessageWithReplyMaxSending extends DirectRequestMessage {

	private static final long serialVersionUID = 6358613094488111567L;

	private final String contentKey;

	public TestDirectMessageWithReplyMaxSending(PeerAddress targetAddress, String contentKey) {
		super(targetAddress);
		this.contentKey = contentKey;
	}

	@Override
	public void run() {
		String secret = NetworkTestUtil.randomString();

		try {
			networkManager
					.getDataManager()
					.putUnblocked(
							new Parameters().setLocationKey(networkManager.getNodeId())
									.setContentKey(contentKey).setData(new H2HTestData(secret)))
					.awaitUninterruptibly();
		} catch (NoPeerConnectionException e) {
			Assert.fail();
		}

		TestResponseMessageMaxSending responseMessage = new TestResponseMessageMaxSending(getMessageID(),
				getSenderAddress(), secret);
		Assert.assertTrue(messageManager.sendDirect(responseMessage, getSenderPublicKey()));
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
						.putUnblocked(
								new Parameters().setLocationKey(networkManager.getNodeId())
										.setContentKey(contentKey).setData(new H2HTestData(receivedSecret)))
						.awaitUninterruptibly();
			} catch (NoPeerConnectionException e) {
				Assert.fail();
			}
		}

	}

}
