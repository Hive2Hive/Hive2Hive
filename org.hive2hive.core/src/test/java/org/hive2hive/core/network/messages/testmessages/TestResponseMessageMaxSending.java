package org.hive2hive.core.network.messages.testmessages;

import java.io.Serializable;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.network.messages.AcceptanceReply;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;

/**
 * Test message to simulate rejecting requesting receiver nodes. For further details see
 * {@link TestMessageWithReplyMaxSending}.
 * 
 * @author Seppi
 */
public class TestResponseMessageMaxSending extends ResponseMessage {

	private static final long serialVersionUID = 9169072033631718522L;
	private static int numFails;

	public TestResponseMessageMaxSending(String messageID, PeerAddress requesterAddress, Serializable someContent) {
		super(messageID, requesterAddress, someContent);
		numFails = 0;
	}

	@Override
	public AcceptanceReply accept() {
		// reject all messages till last try
		if (++numFails < H2HConstants.MAX_MESSAGE_SENDING_DIRECT) {
			return AcceptanceReply.FAILURE;
		}
		return super.accept();
	}

}
