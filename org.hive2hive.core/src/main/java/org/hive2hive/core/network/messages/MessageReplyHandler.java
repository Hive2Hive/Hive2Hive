package org.hive2hive.core.network.messages;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.SignatureException;

import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.HybridEncryptedContent;
import org.hive2hive.core.security.IH2HEncryption;
import org.hive2hive.core.security.SerializationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static final Logger logger = LoggerFactory.getLogger(MessageReplyHandler.class);

	private final NetworkManager networkManager;
	private final IH2HEncryption encryption;

	public MessageReplyHandler(NetworkManager networkManager, IH2HEncryption encryption) {
		this.networkManager = networkManager;
		this.encryption = encryption;
	}

	@Override
	public Object reply(PeerAddress sender, Object request) {
		if (!(request instanceof HybridEncryptedContent)) {
			logger.error("Received unknown object.");
			return null;
		}

		H2HSession session;
		try {
			if (networkManager.getSession() == null) {
				throw new NoSessionException();
			} else {
				session = networkManager.getSession();
			}
		} catch (NoSessionException e) {
			logger.warn("Currently no user is logged in! Keys for decryption needed. Node ID = '{}'.",
					networkManager.getNodeId());
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
			KeyPair keys = session.getKeyPair();
			decryptedMessage = encryption.decryptHybridRaw(encryptedMessage, keys.getPrivate());
		} catch (Exception e) {
			logger.warn("Decryption of message failed.", e);
			return AcceptanceReply.FAILURE_DECRYPTION;
		}

		// deserialize decrypted message
		Object message = null;
		try {
			message = SerializationUtil.deserialize(decryptedMessage);
		} catch (IOException | ClassNotFoundException e) {
			logger.error("Message could not be deserialized.", e);
		}

		if (message != null && message instanceof BaseMessage) {
			BaseMessage receivedMessage = (BaseMessage) message;

			// verify the signature
			if (session.getKeyManager().containsPublicKey(senderId)) {
				if (!verifySignature(senderId, decryptedMessage, signature)) {
					return AcceptanceReply.FAILURE_SIGNATURE;
				}

				// give a network manager reference to work (verify, handle)
				try {
					receivedMessage.setNetworkManager(networkManager);
				} catch (NoPeerConnectionException e) {
					logger.error("Cannot process the message because the peer is not connected.", e);
					return AcceptanceReply.FAILURE;
				}

				// check if message gets accepted
				AcceptanceReply reply = receivedMessage.accept();
				if (AcceptanceReply.OK == reply) {
					// handle message in own thread
					logger.debug("Received and accepted the message. Node ID = '{}'.", networkManager.getNodeId());
					new Thread(receivedMessage).start();
				} else {
					logger.warn("Received but denied a message. Acceptance reply = '{}', Node ID = '{}'.", reply,
							networkManager.getNodeId());
				}

				return reply;
			} else {
				new Thread(new VerifyMessage(senderId, decryptedMessage, signature, receivedMessage)).start();
				return AcceptanceReply.OK_PROVISIONAL;
			}
		} else {
			logger.error("Received unknown object.");
			return null;
		}
	}

	private boolean verifySignature(String senderId, byte[] decryptedMessage, byte[] signature) {
		try {
			PublicKey publicKey = networkManager.getSession().getKeyManager().getPublicKey(senderId);
			if (EncryptionUtil.verify(decryptedMessage, signature, publicKey)) {
				logger.debug("Message signature from user '{}' verified. Node ID = '{}'.", senderId,
						networkManager.getNodeId());
				return true;
			} else {
				logger.error("Message from user '{}' has wrong signature. Node ID = '{}'.", senderId,
						networkManager.getNodeId());
				return false;
			}
		} catch (GetFailedException | InvalidKeyException | SignatureException | NoSessionException e) {
			logger.error("Verifying message from user '{}' failed.", senderId, e);
			return false;
		}
	}

	private class VerifyMessage implements Runnable {

		private final String senderId;
		private final byte[] decryptedMessage;
		private final byte[] signature;
		private final BaseMessage message;

		public VerifyMessage(String senderId, byte[] decryptedMessage, byte[] signature, BaseMessage message) {
			this.senderId = senderId;
			this.decryptedMessage = decryptedMessage;
			this.signature = signature;
			this.message = message;
		}

		@Override
		public void run() {
			if (!verifySignature(senderId, decryptedMessage, signature)) {
				return;
			}

			// give a network manager reference to work (verify, handle)
			try {
				message.setNetworkManager(networkManager);
			} catch (NoPeerConnectionException e) {
				logger.error("Cannot process the message because the peer is not connected.", e);
				return;
			}

			// check if message gets accepted
			AcceptanceReply reply = message.accept();
			if (AcceptanceReply.OK == reply) {
				// handle message in own thread
				logger.debug("Received and accepted the message. Node ID = '{}'.", networkManager.getNodeId());
				new Thread(message).start();
			} else {
				logger.warn("Received but denied a message. Acceptance reply = '{}', Node ID = '{}'.", reply,
						networkManager.getNodeId());
			}
		}

	}
}
