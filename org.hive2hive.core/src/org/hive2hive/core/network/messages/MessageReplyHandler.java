package org.hive2hive.core.network.messages;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.SignatureException;

import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
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
		Object message = null;
		try {
			message = EncryptionUtil.deserializeObject(decryptedMessage);
		} catch (IOException | ClassNotFoundException e) {
			logger.error(String.format("Message could not be deserialized. reason = '%s'", e.getMessage()));
		}

		if (message != null && message instanceof BaseMessage) {
			BaseMessage receivedMessage = (BaseMessage) message;

			// verify the signature
			try {
				PublicKey publicKey = networkManager.getPublicKey(senderId);
				if (EncryptionUtil.verify(decryptedMessage, signature, publicKey)) {
					logger.debug(String.format("Message's signature from user '%s' verified. node id = '%s'",
							senderId, networkManager.getNodeId()));
				} else {
					logger.error(String.format("Message from user '%s' has wrong signature. node id = '%s'",
							senderId, networkManager.getNodeId()));
					return AcceptanceReply.FAILURE_SIGNATURE;
				}
			} catch (GetFailedException | InvalidKeyException | SignatureException e) {
				logger.error(String.format("Verifying message from user '%s' failed. reason = '%s'",
						senderId, e.getMessage()));
				return AcceptanceReply.FAILURE_SIGNATURE;
			}

			// give a network manager reference to work (verify, handle)
			try {
				receivedMessage.setNetworkManager(networkManager);
			} catch (NoPeerConnectionException e) {
				logger.error("Cannot process the message because the peer is not connected");
				return AcceptanceReply.FAILURE;
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
