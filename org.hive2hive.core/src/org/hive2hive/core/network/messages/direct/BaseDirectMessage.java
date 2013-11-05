package org.hive2hive.core.network.messages.direct;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.network.messages.AcceptanceReply;
import org.hive2hive.core.network.messages.BaseMessage;

public abstract class BaseDirectMessage extends BaseMessage {

	private static final long serialVersionUID = 5080812282190501445L;

	private PeerAddress targetPeerAddress;
	private final boolean needsRedirectedSend;

	/**
	 * This is the abstract base class for messages which are sent directly (via TCP) to a target node.
	 * 
	 * @param messageID
	 *            the ID of this message
	 * @param targetKey
	 *            the target key to which this message should be routed
	 * @param targetPeerAddress
	 *            the {@link PeerAddress} of the target node
	 * @param senderAddress
	 *            the peer address of the sender
	 * @param needsRedirectedSend
	 *            flag which indicates if this message should be rerouted if a direct sending
	 *            to the {@link PeerAddress} fails
	 */
	public BaseDirectMessage(String messageID, String targetKey, PeerAddress targetPeerAddress,
			PeerAddress senderAddress, boolean needsRedirectedSend) {
		super(messageID, targetKey, senderAddress);
		this.targetPeerAddress = targetPeerAddress;
		this.needsRedirectedSend = needsRedirectedSend;
	}

	public boolean needsRedirectedSend() {
		return needsRedirectedSend;
	}

	public PeerAddress getTargetAddress() {
		return targetPeerAddress;
	}

	public void setTargetPeerAddress(PeerAddress aTargetPeerAddress) {
		targetPeerAddress = aTargetPeerAddress;
	}

	@Override
	public AcceptanceReply accept() {
		if (networkManager.getPeerAddress().equals(targetPeerAddress)) {
			return AcceptanceReply.OK;
		}
		return AcceptanceReply.WRONG_TARGET;
	}
}
