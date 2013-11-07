package org.hive2hive.core.test.process.common.massages;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.messages.AcceptanceReply;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.network.messages.direct.response.IResponseCallBackHandler;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.network.messages.request.IRequestMessage;
import org.hive2hive.core.test.H2HTestData;
import org.hive2hive.core.test.network.NetworkTestUtil;

/**
 * A test message which is direct and is a request. Used to test response messages and callback handlers. For
 * further detail see
 * {@link BaseDirectMessageProcessStepTest#baseDirectMessageProcessStepTestWithARequestMessage()}
 * 
 * @author Seppi
 */
public class TestDirectMessageWithReply extends BaseDirectMessage implements IRequestMessage {

	private IResponseCallBackHandler callBackHandler;

	private static final long serialVersionUID = 6358613094488111567L;

	private final String contentKey;

	public TestDirectMessageWithReply(String targetKey, PeerAddress targetAddress, String contentKey,
			boolean needsRedirectedSend) {
		super(targetKey, targetAddress, needsRedirectedSend);
		this.contentKey = contentKey;
	}

	@Override
	public IResponseCallBackHandler getCallBackHandler() {
		return callBackHandler;
	}

	@Override
	public void setCallBackHandler(IResponseCallBackHandler aHandler) {
		callBackHandler = aHandler;
	}

	@Override
	public void run() {
		String secret = NetworkTestUtil.randomString();

		networkManager.putLocal(networkManager.getNodeId(), contentKey, new H2HTestData(secret));

		ResponseMessage responseMessage = new ResponseMessage(getMessageID(), getSenderAddress(), secret);
		networkManager.sendDirect(responseMessage);
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
