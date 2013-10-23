package org.hive2hive.core.test.network.messaging;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.network.messages.AcceptanceReply;
import org.hive2hive.core.test.network.data.TestDataWrapper;

/**
 * Test message to simulate rejecting receiver nodes. For further details see
 * {@link BaseMessageTest#testSendingAnAsynchronousMessageWithNoReplyMaxTimesTargetNode()}
 * 
 * @author Seppi
 */
public class TestMessageOneWayMaxSending extends TestMessageOneWay {

	private static final long serialVersionUID = -6955621718515026298L;

	public TestMessageOneWayMaxSending(String targetKey, String contentKey, TestDataWrapper wrapper) {
		super(targetKey, contentKey, wrapper);
	}

	@Override
	public AcceptanceReply accept() {
		if (getSendingCounter() < H2HConstants.MAX_MESSAGE_SENDING) {
			return AcceptanceReply.FAILURE;
		}
		return AcceptanceReply.OK;
	}

}
