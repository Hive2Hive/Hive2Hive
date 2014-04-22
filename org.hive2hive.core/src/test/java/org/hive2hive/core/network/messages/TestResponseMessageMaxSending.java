package org.hive2hive.core.network.messages;

import java.io.Serializable;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;

/**
 * Test message to simulate rejecting requesting receiver nodes. For further details see
 * {@link TestMessageWithReplyMaxSending}.
 * 
 * @author Seppi
 */
public class TestResponseMessageMaxSending extends ResponseMessage {

	private static final long serialVersionUID = 9169072033631718522L;

	public TestResponseMessageMaxSending(String messageID, PeerAddress requesterAddress,
			Serializable someContent) {
		super(messageID, requesterAddress, someContent);
	}

	@Override
	public AcceptanceReply accept() {
		// reject all messages till last try
		if (getDirectSendingCounter() < H2HConstants.MAX_MESSAGE_SENDING_DIRECT) {
			return AcceptanceReply.FAILURE;
		}
		return super.accept();
	}

}
