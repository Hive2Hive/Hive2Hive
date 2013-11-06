package org.hive2hive.core.test.network.messaging;

import java.io.Serializable;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.network.messages.AcceptanceReply;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;

public class TestResponseMessageMaxSending extends ResponseMessage {

	private static final long serialVersionUID = 9169072033631718522L;

	public TestResponseMessageMaxSending(String messageID, String targetKey, PeerAddress senderAddress,
			PeerAddress requesterAddress, Serializable someContent) {
		super(messageID, targetKey, senderAddress, requesterAddress, someContent);
	}

	@Override
	public AcceptanceReply accept() {
		// block the first tries
		if (getDirectSendingCounter() < H2HConstants.MAX_MESSAGE_SENDING_DIRECT) {
			return AcceptanceReply.FAILURE;
		}
		return super.accept();
	}

}
