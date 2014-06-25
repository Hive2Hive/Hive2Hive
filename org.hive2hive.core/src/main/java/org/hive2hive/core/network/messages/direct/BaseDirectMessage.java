package org.hive2hive.core.network.messages.direct;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.network.messages.AcceptanceReply;
import org.hive2hive.core.network.messages.BaseMessage;
import org.hive2hive.core.network.messages.MessageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the base class of all messages used by <code>Hive2Hive</code> which has to be sent directly to
 * another node. All messages are sent to their destination in an asynchronous manner.<br>
 * </br>
 * 
 * <b>Design decision:</b>
 * <ul>
 * <li>The parameter needsRedirectedSend in the constructor enables the fall back mechanism in case sending
 * failed. The direct message is then sent like a {@link BaseMessage} which gets routed to the next
 * responsible peer.</li>
 * <li>Besides the {@link BaseDirectMessage#getSendingCounter()} counter there is a
 * {@link BaseDirectMessage#getDirectSendingCounter()}. The {@link MessageManager} increments the direct
 * counter so that in case of a fail the direct message gets re-send according the
 * {@link H2HConstants#MAX_MESSAGE_SENDING_DIRECT} constant.</li>
 * <li>For further details see also {@link BaseMessage}</li>
 * </ul>
 * 
 * Direct messages are sent by the {@link MessageManager}. For more details please have
 * a look at {@link MessageManager#sendDirect(BaseDirectMessage)}.</br></br>
 * 
 * @author Nendor, Seppi, Nico
 */
public abstract class BaseDirectMessage extends BaseMessage {

	private static final Logger logger = LoggerFactory.getLogger(BaseDirectMessage.class);

	private static final long serialVersionUID = 5080812282190501445L;

	private final PeerAddress targetAddress;
	private final boolean needsRedirectedSend;

	private int directSendingCounter = 0;

	/**
	 * This is the abstract base class for messages which are sent directly (via TCP) to a target node.
	 * 
	 * @param messageID
	 *            the ID of this message
	 * @param targetKey
	 *            the target key to which this message should be routed
	 * @param targetAddress
	 *            the {@link PeerAddress} of the target node
	 * @param needsRedirectedSend
	 *            flag which indicates if this message should be rerouted if a direct sending
	 *            to the {@link PeerAddress} fails
	 */
	public BaseDirectMessage(String messageID, String targetKey, PeerAddress targetAddress, boolean needsRedirectedSend) {
		super(messageID, targetKey);
		this.targetAddress = targetAddress;
		this.needsRedirectedSend = needsRedirectedSend;
	}

	/**
	 * This is the abstract base class for messages which are sent directly (via TCP) to a target node.</br>
	 * A message ID will be created.
	 * 
	 * @param targetKey
	 *            the target key to which this message should be routed
	 * @param targetAddress
	 *            the {@link PeerAddress} of the target node
	 * @param needsRedirectedSend
	 *            flag which indicates if this message should be rerouted if a direct sending
	 *            to the {@link PeerAddress} fails
	 */
	public BaseDirectMessage(String targetKey, PeerAddress targetAddress, boolean needsRedirectedSend) {
		super(createMessageID(), targetKey);
		this.targetAddress = targetAddress;
		this.needsRedirectedSend = needsRedirectedSend;
	}

	/**
	 * This is the abstract base class for messages which are sent directly (via TCP) to a target node.</br>
	 * No targetKey has to be specified and no redirected sending is done in case of failure. </br>
	 * A message ID will be created.
	 * 
	 * @param targetAddress
	 *            the {@link PeerAddress} of the target.
	 */
	public BaseDirectMessage(PeerAddress targetAddress) {
		this(createMessageID(), null, targetAddress, false);
	}

	/**
	 * A flag which indicates if a the direct message wants to be re-send, using the normal routing mechanism
	 * of {@link MessageManager#send(BaseMessage)}.
	 * 
	 * @return <code>true</code> if rerouted sending is wished, <code>false</code> if not
	 */
	public boolean needsRedirectedSend() {
		if (targetKey == null) {
			return false;
		}
		return needsRedirectedSend;
	}

	public PeerAddress getTargetAddress() {
		return targetAddress;
	}

	public int getDirectSendingCounter() {
		return directSendingCounter;
	}

	/**
	 * Increases the internal sending counter of this direct message.
	 */
	public final void increaseDirectSendingCounter() {
		directSendingCounter++;
	}

	@Override
	public AcceptanceReply accept() {
		if (networkManager.getConnection().getPeer().getPeerAddress().equals(targetAddress)) {
			return AcceptanceReply.OK;
		}
		return AcceptanceReply.WRONG_TARGET;
	}

	@Override
	public boolean handleSendingFailure(AcceptanceReply reply) throws IllegalArgumentException {
		logger.debug("Have to handle a sending failure. Reply = '{}'.", reply);
		switch (reply) {
			case WRONG_TARGET:
				logger.error("Wrong node responded while sending this message directly using the peer address '{}'.",
						getTargetAddress());
				return canResend();
			case FAILURE:
				return canResend();
			case FUTURE_FAILURE:
				return canResend();
			case FAILURE_DECRYPTION:
				logger.warn("Message not accepted by the target. Decryption on target node failed. Peer address = '{}'.",
						getTargetAddress());
				return false;
			case FAILURE_SIGNATURE:
				logger.warn("Message not accepted by the target. Signature is wrong. Peer address = '{}'.",
						getTargetAddress());
				return false;
			case OK:
				logger.error("Trying to handle a AcceptanceReply.OK as a failure.");
				throw new IllegalArgumentException("AcceptanceReply.OK is not a failure.");
			default:
				logger.error("Unkown AcceptanceReply argument: {}.", reply);
				throw new IllegalArgumentException(String.format("Unkown AcceptanceReply argument: %s", reply));
		}
	}

	private boolean canResend() {
		return directSendingCounter < H2HConstants.MAX_MESSAGE_SENDING_DIRECT;
	}
}