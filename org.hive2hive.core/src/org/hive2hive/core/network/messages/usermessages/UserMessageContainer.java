package org.hive2hive.core.network.messages.usermessages;

import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.network.data.NetworkContentWrapper;

/**
 * Holds the user message bytes and its signature. This class wraps those two parameters and can be stored< as
 * {@link NetworkContent}.
 * 
 * @author Christian
 * 
 */
public class UserMessageContainer extends NetworkContentWrapper<byte[]> {

	private static final long serialVersionUID = -2450838549852395277L;

	private final byte[] signature;

	public UserMessageContainer(byte[] messageBytes, byte[] signature) {
		super(messageBytes);
		this.signature = signature;
	}

	public byte[] getMessageBytes() {
		return getContent();
	}

	// TODO rename to getSignature()
	public byte[] getMessageSignature() {
		return signature;
	}
}
