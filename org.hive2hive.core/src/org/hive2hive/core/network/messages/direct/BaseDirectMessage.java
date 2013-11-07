package org.hive2hive.core.network.messages.direct;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.messages.AcceptanceReply;
import org.hive2hive.core.network.messages.BaseMessage;
import org.hive2hive.core.network.messages.MessageManager;

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
 * <li>Beside the {@link BaseDirectMessage#getSendingCounter()} counter there is a
 * {@link BaseDirectMessage#getDirectSendingCounter()}. The {@link MessageManager} increments the direct
 * counter so that in case of a fail the direct message gets re-send according the
 * {@link H2HConstants#MAX_MESSAGE_SENDING_DIRECT} constant.</li>
 * <li>For further details see also {@link BaseMessage}</li>
 * </ul>
 * 
 * Direct messages are sent by the {@link MessageManager}. For more details please have
 * a look at {@link MessageManager#sendDirect(BaseDirectMessage)}.</br></br>
 * 
 * @author Nendor, Seppi
 */
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
	 * @param needsRedirectedSend
	 *            flag which indicates if this message should be rerouted if a direct sending
	 *            to the {@link PeerAddress} fails
	 */
	public BaseDirectMessage(String messageID, String targetKey, PeerAddress targetPeerAddress,
			boolean needsRedirectedSend) {
		super(messageID, targetKey);
		this.targetPeerAddress = targetPeerAddress;
		this.needsRedirectedSend = needsRedirectedSend;
	}

	/**
	 * This is the abstract base class for messages which are sent directly (via TCP) to a target node. A
	 * message id will be created.
	 * 
	 * @param targetKey
	 *            the target key to which this message should be routed
	 * @param targetPeerAddress
	 *            the {@link PeerAddress} of the target node
	 * @param needsRedirectedSend
	 *            flag which indicates if this message should be rerouted if a direct sending
	 *            to the {@link PeerAddress} fails
	 */
	public BaseDirectMessage(String targetKey, PeerAddress targetPeerAddress, boolean needsRedirectedSend) {
		super(createMessageID(), targetKey);
		this.targetPeerAddress = targetPeerAddress;
		this.needsRedirectedSend = needsRedirectedSend;
	}

	/**
	 * A flag which indicates if a the direct message wants to be re-send, using the normal routing mechanism
	 * of {@link MessageManager#send(BaseMessage)}.
	 * 
	 * @return <code>true</code> if rerouted sending is wished, <code>false</code> if not
	 */
	public boolean needsRedirectedSend() {
		if (targetKey == null)
			return false;
		return needsRedirectedSend;
	}

	public PeerAddress getTargetAddress() {
		return targetPeerAddress;
	}

	public int getDirectSendingCounter() {
		return directSendingCounter;
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

}
