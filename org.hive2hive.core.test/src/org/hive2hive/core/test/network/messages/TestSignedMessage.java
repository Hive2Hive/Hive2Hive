package org.hive2hive.core.test.network.messages;

import java.security.PublicKey;

import org.hive2hive.core.network.messages.AcceptanceReply;
import org.hive2hive.core.network.messages.BaseMessage;

public class TestSignedMessage extends BaseMessage {

	private static final long serialVersionUID = -7416023464387691292L;

	private final PublicKey publicKey;
	
	public TestSignedMessage(String targetKey, PublicKey publicKey) {
		super(createMessageID(), targetKey);
		this.publicKey = publicKey;
	}

	@Override
	public void run() {
	}

	@Override
	public AcceptanceReply accept() {
		return AcceptanceReply.OK;
	}

	@Override
	public boolean checkSignature() {
		return verify(publicKey);
	}

}