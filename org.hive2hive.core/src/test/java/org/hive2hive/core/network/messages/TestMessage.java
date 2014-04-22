package org.hive2hive.core.network.messages;

import org.hive2hive.core.H2HTestData;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.junit.Assert;

/**
 * This test message is used to put locally some content into the target node where the location key is equals
 * the node id of receiver node. This behavior is used to check if this message is actually sent to the target
 * and executed there successfully. For further details see
 * {@link BaseMessageTest#testSendingAnAsynchronousMessageWithNoReplyToTargetNode()}
 * 
 * @author Seppi
 */
public class TestMessage extends BaseMessage {

	private static final long serialVersionUID = 880089170139661640L;

	private final String contentKey;
	private final H2HTestData wrapper;

	public TestMessage(String targetKey, String contentKey, H2HTestData wrapper) {
		super(createMessageID(), targetKey);
		this.contentKey = contentKey;
		this.wrapper = wrapper;
	}

	@Override
	public void run() {
		try {
			networkManager
					.getDataManager()
					.putUnblocked(
							new Parameters().setLocationKey(networkManager.getNodeId())
									.setContentKey(contentKey).setData(wrapper)).awaitUninterruptibly();
		} catch (NoPeerConnectionException e) {
			Assert.fail();
		}
	}

	@Override
	public AcceptanceReply accept() {
		return AcceptanceReply.OK;
	}

}
