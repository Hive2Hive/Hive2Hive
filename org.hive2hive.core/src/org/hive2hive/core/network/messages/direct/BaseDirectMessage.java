package org.hive2hive.core.network.messages.direct;

import java.util.List;

import net.tomp2p.futures.FutureResponse;
import net.tomp2p.message.Buffer;
import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.messages.AcceptanceReply;
import org.hive2hive.core.network.messages.BaseMessage;

public abstract class BaseDirectMessage extends BaseMessage {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(BaseDirectMessage.class);

	private static final long serialVersionUID = 5080812282190501445L;

	private PeerAddress targetPeerAddress;
	private final boolean needsRedirectedSend;

	private int directSendingCounter = 0;

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

	/**
	 * Increases the internal sending counter of this direct message.
	 */
	public void increaseDirectSendingCounter() {
		directSendingCounter++;
	}

	@Override
	public AcceptanceReply accept() {
		if (networkManager.getPeerAddress().equals(targetPeerAddress)) {
			return AcceptanceReply.OK;
		}
		return AcceptanceReply.WRONG_TARGET;
	}

	@Override
	public boolean handleSendingFailure(AcceptanceReply reply) throws IllegalArgumentException {
		logger.debug(String.format("Have to handle a sending failure. reply = '%s'", reply));
		switch (reply) {
			case WRONG_TARGET:
				logger.error(String
						.format("Wrong node responded while sending this message directly using the peer address '%s' ",
								getTargetAddress()));
			case FAILURE:
			case FUTURE_FAILURE:
				if (directSendingCounter < H2HConstants.MAX_MESSAGE_SENDING_DIRECT) {
					return true;
				} else {
					logger.debug(String.format(
							"Failure while sending this message directly using the peer address '%s' ",
							getTargetAddress()));
					return false;
				}
			case OK:
				logger.error("Trying to handle a AcceptanceReply.OK as a failure.");
				throw new IllegalArgumentException("AcceptanceReply.OK is not a failure.");
			default:
				logger.error(String.format("Unkown AcceptanceReply argument: %s", reply));
				throw new IllegalArgumentException(
						String.format("Unkown AcceptanceReply argument: %s", reply));
		}
	}

	public AcceptanceReply extractAcceptanceReply(FutureResponse aFuture) {
		String errorReason = "";
		if (aFuture.isSuccess()) {
			List<Buffer> returnedBuffer = aFuture.getResponse().getBufferList();
			if (returnedBuffer == null) {
				errorReason = "Returned buffer is null.";
			} else if (returnedBuffer.isEmpty()) {
				errorReason = "Returned buffer is empty.";
			} else {
				Buffer firstReturnedBuffer = returnedBuffer.iterator().next();
				if (firstReturnedBuffer == null) {
					errorReason = "First returned buffer is null.";
				} else {
					Object responseObject;
					try {
						responseObject = firstReturnedBuffer.object();
						if (responseObject instanceof AcceptanceReply) {
							AcceptanceReply reply = (AcceptanceReply) responseObject;
							return reply;
						} else {
							errorReason = "The returned object was not of type AcceptanceReply!";
						}
					} catch (Exception e) {
						errorReason = "Exception occured while getting the object.";
					}
				}
			}
			logger.error(String.format("A failure while sending a message occured. reason = '%s'",
					errorReason));
			return AcceptanceReply.FAILURE;
		} else {
			logger.error(String.format("Future not successful. reason = '%s'", aFuture.getFailedReason()));
			return AcceptanceReply.FUTURE_FAILURE;
		}
	}
}
