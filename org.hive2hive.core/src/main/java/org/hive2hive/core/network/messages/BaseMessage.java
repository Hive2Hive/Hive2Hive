package org.hive2hive.core.network.messages;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.PublicKey;
import java.security.SecureRandom;

import net.tomp2p.futures.FutureDHT;
import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.network.NetworkManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the base class of all messages used by <code>Hive2Hive</code>.</br>
 * All messages are sent to their destination in an asynchronous manner.<br>
 * </br>
 * 
 * <b>Design decision:</b></br> All messages in <code>Hive2Hive</code> have to
 * be send asynchronously. The reason is simply to avoid timeouts caused by
 * blocking threads which wait for the messages return/completion. If we wait
 * for a message to return and block the current thread (the synchronous way of
 * doing it) the current node itself is not able to reply on incoming messages.
 * This leads to timeouts for requesters and as a consequence to messages routed
 * to the 'wrong' peer/node because <code>TomP2P</code> thinks the current node
 * has left the network...</br></br>
 * 
 * Messages are sent by the {@link MessageManager}. For more details please have
 * a look at {@link MessageManager#send(BaseMessage)}.
 * 
 * @author Nendor, Seppi
 * 
 */
public abstract class BaseMessage implements Runnable, Serializable {

	private static final long serialVersionUID = 2885498439522219441L;

	private static final Logger logger = LoggerFactory.getLogger(BaseMessage.class);

	protected NetworkManager networkManager;
	protected MessageManager messageManager;
	protected PublicKey senderPublicKey;

	protected final String messageID;
	protected final String targetKey;
	private final SendingBehavior sendingBehavior;

	protected PeerAddress senderAddress;

	private int routedSendingCounter = 0;

	/**
	 * Constructor for an asynchronous message.
	 * 
	 * @param messageID
	 *            the ID of this message - should be chosen uniquely if possible
	 * @param targetKey
	 *            the key identifying the target of this message
	 * @param sendingBehavior
	 *            the sending behavior used for this message
	 */
	public BaseMessage(String messageID, String targetKey, SendingBehavior sendingBehavior) {
		this.messageID = messageID;
		this.targetKey = targetKey;
		this.sendingBehavior = sendingBehavior;
	}

	/**
	 * Constructor for an asynchronous message.</br> This constructor creates an
	 * asynchronous message with the default sending behavior {@link SendingBehavior#SEND_MAX_ALLOWED_TIMES}.
	 * 
	 * @param messageID
	 *            the ID of this message - should be chosen uniquely if possible
	 * @param targetKey
	 *            the key identifying the target of this message
	 */
	public BaseMessage(String messageID, String targetKey) {
		this(messageID, targetKey, SendingBehavior.SEND_MAX_ALLOWED_TIMES);
	}

	/**
	 * Constructor for an asynchronous message.</br>
	 * A message ID is generated.</br>
	 * This constructor creates an asynchronous message with the default sending behavior
	 * {@link SendingBehavior#SEND_MAX_ALLOWED_TIMES}.
	 * 
	 * @param targetKey
	 *            the key identifying the target of this message
	 */
	public BaseMessage(String targetKey) {
		this(createMessageID(), targetKey, SendingBehavior.SEND_MAX_ALLOWED_TIMES);
	}

	/**
	 * Getter
	 * 
	 * @return the ID of this message
	 */
	public String getMessageID() {
		return messageID;
	}

	/**
	 * Getter
	 * 
	 * @return the target key of this message
	 */
	public String getTargetKey() {
		return targetKey;
	}

	/**
	 * Getter
	 * 
	 * @return peer address of the sender
	 */
	public PeerAddress getSenderAddress() {
		return senderAddress;
	}

	/**
	 * Getter
	 * 
	 * @return public key of the sender
	 */
	public PublicKey getSenderPublicKey() {
		return senderPublicKey;
	}

	/**
	 * Getter
	 * 
	 * @return the current value of this messages sending counter
	 */
	public int getSendingCounter() {
		return routedSendingCounter;
	}

	/**
	 * Getter
	 * 
	 * @return the {@link SendingBehavior} for this message
	 */
	public SendingBehavior getSendingBehavior() {
		return sendingBehavior;
	}

	/**
	 * Setter
	 * 
	 * @param senderAddress
	 *            the peer address of the sender node
	 */
	public void setSenderAddress(PeerAddress senderAddress) {
		this.senderAddress = senderAddress;
	}

	/**
	 * Setter
	 * 
	 * @param senderPublicKey
	 *            the public key of the sender node
	 */
	public void setSenderPublicKey(PublicKey senderPublicKey) {
		this.senderPublicKey = senderPublicKey;
	}

	/**
	 * Setter
	 * 
	 * @param aNetworkManager
	 *            the {@link NetworkManager} to be used by this message
	 * @throws NoPeerConnectionException
	 */
	public void setNetworkManager(NetworkManager networkManager) throws NoPeerConnectionException {
		this.networkManager = networkManager;
		this.messageManager = networkManager.getMessageManager();
	}

	/**
	 * Increases the internal sending counter of this message.
	 */
	public void increaseRoutedSendingCounter() {
		routedSendingCounter++;
	}

	/**
	 * This method is called on the receiver node (the one responsible for {@link #targetKey}) of this
	 * message. It is used to check if the target node is able/willing to handle this message at all.</br>
	 * 
	 * For more information on how the value generated by this method is used
	 * see {@link MessageReplyHandler#reply(net.tomp2p.peers.PeerAddress, Object)} and
	 * {@link MessageManager.FutureListener#extractAcceptanceReply(FutureDHT)} &
	 * {@link MessageManager.FutureListener2#extractAcceptanceReply(FutureDHT)}.
	 * </br></br>
	 * 
	 * <b>Important:</b></br> All concrete subclasses have to implement this
	 * method and adhere to the following points:</br>
	 * <ul>
	 * <li>This method must terminate as quickly as possible.</li>
	 * <li>No blocking in any way is allowed.</li>
	 * <li>No sending of messages or other network activities are permitted.</li>
	 * </ul>
	 * 
	 * @return the {@link AcceptanceReply} of the target node.
	 */
	public abstract AcceptanceReply accept();

	/**
	 * This method is called if a failure is detected while sending this message. The idea is that sending
	 * failures are handled by the message itself, because the message is the only entity which knows how to
	 * perform from the point of a failure on. Some messages don't need do be sent again, while others need a
	 * redirect or wish to inform special (potential several) customers about the failure.</br>
	 * This abstract class implements the default behavior for {@link SendingBehavior#SEND_MAX_ALLOWED_TIMES}.
	 * Subclasses which need a different or more elaborated failure handling have to extend or override this
	 * method.</br>
	 * </br>
	 * This method analyzes the reply and give <code>true</code> if a resend of the message is recommend and a
	 * <code>false</code> if not.
	 * 
	 * @param reply
	 *            the reply of the sending attempts
	 * @return <code>true</code> if resending recommended, <code>false</code> if not
	 * @throws IllegalArgumentException reply has to be {@link AcceptanceReply#FAILURE} or
	 *             {@link AcceptanceReply#FUTURE_FAILURE}
	 */
	public boolean handleSendingFailure(AcceptanceReply reply) throws IllegalArgumentException {
		logger.debug("Have to handle a sending failure. Reply = '{}'.", reply);
		switch (reply) {
			case FAILURE:
			case FUTURE_FAILURE:
				if (SendingBehavior.SEND_MAX_ALLOWED_TIMES == sendingBehavior) {
					if (routedSendingCounter < H2HConstants.MAX_MESSAGE_SENDING) {
						return true;
					} else {
						logger.error(
								"Message did not get accepted by the targets in {} tries. Target key = '{}'.",
								routedSendingCounter, targetKey);
						return false;
					}
				} else {
					logger.warn("Message not accepted by the target after one try. Target key = '{}'.",
							targetKey);
					return false;
				}
			case FAILURE_DECRYPTION:
				logger.warn(
						"Message not accepted by the target. Decryption on target node failed. Target key = '{}'.",
						targetKey);
				return false;
			case FAILURE_SIGNATURE:
				logger.warn("Message not accepted by the target. Signature is wrong. Target key = '{}'.",
						targetKey);
				return false;
			case OK:
				logger.error("Trying to handle an AcceptanceReply.OK as a failure.");
				throw new IllegalArgumentException("AcceptanceReply.OK is not a failure.");
			default:
				logger.error("Unkown AcceptanceReply argument: {}.", reply);
				throw new IllegalArgumentException(
						String.format("Unkown AcceptanceReply argument: %s.", reply));
		}
	}

	/**
	 * Convenience method to create a random message ID
	 * 
	 * @return a random String of 12 characters length
	 */
	protected static String createMessageID() {
		return new BigInteger(56, new SecureRandom()).toString(32);
	}

}
