package org.hive2hive.core.test.network.messages;

import org.hive2hive.core.network.messages.AcceptanceReply;
import org.hive2hive.core.network.messages.BaseMessage;
import org.hive2hive.core.test.H2HTestData;

/**
 * This test message is used to put locally some content into the target node where the location key is equals
 * the node id of receiver node. This behavior is used to check if this message is actually sent to the target
 * and executed there successfully. For further details see
 * {@link BaseMessageTest#testSendingAnAsynchronousMessageWithNoReplyToTargetNode()}
 * 
 * @author Seppi
 * 
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
		networkManager.getDataManager().putLocal(networkManager.getNodeId(), contentKey, wrapper);
	}

	@Override
	public AcceptanceReply accept() {
		return AcceptanceReply.OK;
	}

}
