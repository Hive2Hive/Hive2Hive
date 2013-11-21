package org.hive2hive.core.network.messages.usermessages;

import org.hive2hive.core.TimeToLiveStore;
import org.hive2hive.core.network.data.NetworkContent;

/**
 * Holds the user message bytes and its signature. This class wraps those two parameters and can be stored< as
 * {@link NetworkContent}.
 * 
 * @author Christian
 * 
 */
public class UserMessageContainer extends NetworkContent{

	private static final long serialVersionUID = -2450838549852395277L;

	private final byte[] signature;
	private final byte[] messageBytes;

	public UserMessageContainer(byte[] messageBytes, byte[] signature) {
		this.messageBytes = messageBytes;
		this.signature = signature;
	}

	public byte[] getMessageBytes() {
		return messageBytes;
	}

	// TODO rename to getSignature()
	public byte[] getMessageSignature() {
		return signature;
	}

	@Override
	public int getTimeToLive() {
		return TimeToLiveStore.getInstance().getUserMessageQueue();
	}
}
