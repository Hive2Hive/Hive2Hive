package org.hive2hive.core.network.messages.direct;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.messages.AcceptanceReply;
import org.hive2hive.core.network.messages.BaseMessage;
import org.hive2hive.core.network.messages.SendingBehavior;

public abstract class BaseDirectMessage extends BaseMessage {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(BaseDirectMessage.class);

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

	@Override
	public boolean handleSendingFailure(AcceptanceReply reply) {
		logger.debug(String.format("Have to handle a sending failure. AcceptanceReply='%s'", reply));
		switch (reply) {
			case OK:
				logger.error("Trying to handle a AcceptanceReply.OK as a failure.");
				throw new IllegalArgumentException("AcceptanceReply.OK is not a failure.");
			case FAILURE:
			case FUTURE_FAILURE:
			case WRONG_TARGET:
				if (SendingBehavior.SEND_MAX_ALLOWED_TIMES == getSendingBehavior()) {
					if (getSendingCounter() < H2HConstants.MAX_MESSAGE_SENDING) {
						logger.warn(String.format("Message reply was failure. Resending #%s...",
								getSendingCounter()));
						return true;
					} else {
						logger.error(String
								.format("Message does not getting accepted by the targets in %d tries. target key = '%s'",
										getSendingCounter(), getTargetKey()));
						return false;
					}
				} else {
					logger.warn(String
							.format("Message not accepted by the target after one try. Details:\n target key = '%s' message id = '%s'",
									getTargetKey(), getMessageID()));
					return false;
				}
			default:
				logger.error(String.format("Unkown AcceptanceReply argument: %s", reply));
				throw new IllegalArgumentException(
						String.format("Unkown AcceptanceReply argument: %s", reply));
		}
	}
}
