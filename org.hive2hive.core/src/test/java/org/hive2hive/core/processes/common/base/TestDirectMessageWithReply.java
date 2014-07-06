package org.hive2hive.core.processes.common.base;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.H2HTestData;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.network.NetworkTestUtil;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.network.messages.AcceptanceReply;
import org.hive2hive.core.network.messages.request.DirectRequestMessage;
import org.junit.Assert;

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

		try {
			networkManager.getDataManager().putUnblocked(
					new Parameters().setLocationKey(networkManager.getNodeId())
					.setContentKey(contentKey).setData(new H2HTestData(secret)))
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

}
