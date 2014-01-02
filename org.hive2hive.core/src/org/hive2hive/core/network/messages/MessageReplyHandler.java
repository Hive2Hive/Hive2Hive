package org.hive2hive.core.network.messages;

import java.security.KeyPair;

import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;

import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.HybridEncryptedContent;

/**
 * This is the general message handler of each node. It checks if received
 * message is ok (depends on message e.g. routed to correct node). If accepted
 * the message gets independently handled in a own thread. As soon as the
 * handler thread has started the reply handler gives immediately response to
 * the sender node. This design allows a quick and non-blocking message
 * handling.
 * 
 * @author Nendor, Seppi
 */
public class MessageReplyHandler implements ObjectDataReply {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(MessageReplyHandler.class);

	private final NetworkManager networkManager;

	public MessageReplyHandler(NetworkManager networkManager) {
		this.networkManager = networkManager;
	}

	@Override
	public Object reply(PeerAddress sender, Object request) {
		if (!(request instanceof HybridEncryptedContent)) {
			logger.error("Received unknown object.");
			return null;
		}

		try {
			if (networkManager.getSession() == null) {
				throw new NoSessionException();
			}
		} catch (NoSessionException e) {
			logger.warn(String.format(
					"Currently no user logged in! Keys for decryption needed. node id = '%s'",
					networkManager.getNodeId()));
			return AcceptanceReply.FAILURE;
		}

		HybridEncryptedContent encryptedMessage = (HybridEncryptedContent) request;

		// get signature
		String senderId = encryptedMessage.getUserId();
		byte[] signature = encryptedMessage.getSignature();
		if (senderId == null || signature == null) {
			logger.warn("No signature for message.");
			return AcceptanceReply.FAILURE_SIGNATURE;
		}

		// asymmetrically decrypt message
		byte[] decryptedMessage = null;
		try {
			KeyPair keys = networkManager.getSession().getKeyPair();
			decryptedMessage = EncryptionUtil.decryptHybrid(encryptedMessage, keys.getPrivate());
		} catch (Exception e) {
			logger.warn("Decryption of message failed.");
			return AcceptanceReply.FAILURE_DECRYPTION;
		}

		// deserialize decrypted message
		Object message = EncryptionUtil.deserializeObject(decryptedMessage);

		if (message != null && message instanceof BaseMessage) {
			BaseMessage receivedMessage = (BaseMessage) message;
			byte[] data = EncryptionUtil.serializeObject(receivedMessage);

			// give a network manager reference to work (verify, handle)
			receivedMessage.setNetworkManager(networkManager);

			// verify the signature
			if (!receivedMessage.checkSignature(data, signature, senderId)) {
				logger.error(String.format("Message has wrong signature. node id = '%s'",
						networkManager.getNodeId()));
				return AcceptanceReply.FAILURE_SIGNATURE;
			} else {
				logger.debug(String.format("Message's signature verified. node id = '%s'",
						networkManager.getNodeId()));
			}

			// check if message gets accepted
			AcceptanceReply reply = receivedMessage.accept();
			if (AcceptanceReply.OK == reply) {
				// handle message in own thread
				logger.debug(String.format("Received and accepted the message. node id = '%s'",
						networkManager.getNodeId()));
				new Thread(receivedMessage).start();
			} else {
				logger.warn(String.format(
						"Received but denied a message. Acceptance reply = '%s' node id = '%s'", reply,
						networkManager.getNodeId()));
			}

			return reply;
		} else {
			logger.error("Received unknown object.");
			return null;
		}
	}
}
