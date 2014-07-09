package org.hive2hive.core.network.messages;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import net.tomp2p.futures.FutureDirect;
import net.tomp2p.futures.FutureSend;
import net.tomp2p.p2p.RequestP2PConfiguration;
import net.tomp2p.peers.Number160;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HSession;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.network.messages.direct.response.IResponseCallBackHandler;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.network.messages.futures.FutureDirectListener;
import org.hive2hive.core.network.messages.futures.FutureRoutedListener;
import org.hive2hive.core.network.messages.request.IRequestMessage;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.HybridEncryptedContent;
import org.hive2hive.core.security.IH2HEncryption;
import org.hive2hive.core.security.SerializationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles the sending of messages.
 * 
 * @author Seppi
 */
public final class MessageManager implements IMessageManager {

	private static final Logger logger = LoggerFactory.getLogger(MessageManager.class);

	private final NetworkManager networkManager;
	private final Map<String, IResponseCallBackHandler> callBackHandlers;
	private final IH2HEncryption encryption;

	public MessageManager(NetworkManager networkManager, IH2HEncryption encryption) {
		this.networkManager = networkManager;
		this.encryption = encryption;
		this.callBackHandlers = new HashMap<String, IResponseCallBackHandler>();
	}

	@Override
	public boolean send(BaseMessage message, PublicKey targetPublicKey) {
		if (message.getTargetKey() == null) {
			throw new IllegalArgumentException("target key cannot be null");
		}
		if (targetPublicKey == null) {
			throw new IllegalArgumentException("target public key cannot be null");
		}

		// prepare message
		prepareMessage(message);
		message.increaseRoutedSendingCounter();

		// encrypt the message with the given public key
		HybridEncryptedContent encryptedMessage = signAndEncryptMessage(message, targetPublicKey);
		if (encryptedMessage == null) {
			return false;
		}

		// send message to the peer which is responsible for the given key
		FutureSend futureSend = networkManager.getConnection().getPeer().send(Number160.createHash(message.getTargetKey()))
				.setObject(encryptedMessage).setRequestP2PConfiguration(createSendingConfiguration()).start();

		// attach a future listener to log, handle and notify events
		FutureRoutedListener listener = new FutureRoutedListener(message, targetPublicKey, this);
		futureSend.addListener(listener);
		boolean success = listener.await();

		if (success) {
			logger.debug("Message sent. Target key = '{}', Message ID = '{}'.", message.getTargetKey(),
					message.getMessageID());
		} else {
			logger.error("Message could not be sent. Target key = '{}', Message ID = '{}'.", message.getTargetKey(),
					message.getMessageID());
		}
		return success;
	}

	@Override
	public boolean sendDirect(BaseDirectMessage message, PublicKey targetPublicKey) {
		if (message.getTargetAddress() == null) {
			throw new IllegalArgumentException("Target address cannot be null.");
		}
		if (targetPublicKey == null) {
			throw new IllegalArgumentException("Target public key cannot be null.");
		}

		// prepare message
		prepareMessage(message);
		message.increaseDirectSendingCounter();

		// encrypt the message with the given public key
		HybridEncryptedContent encryptedMessage = signAndEncryptMessage(message, targetPublicKey);
		if (encryptedMessage == null) {
			return false;
		}

		// send message directly to the peer with the given peer address
		FutureDirect futureDirect = networkManager.getConnection().getPeer().sendDirect(message.getTargetAddress())
				.setObject(encryptedMessage).start();
		// attach a future listener to log, handle and notify events
		FutureDirectListener listener = new FutureDirectListener(message, targetPublicKey, this);
		futureDirect.addListener(listener);
		boolean success = listener.await();

		if (success) {
			logger.debug("Message (direct) sent. Message ID = '{}', Target address = '{}', Sender address = '{}'.",
					message.getMessageID(), message.getTargetAddress(), message.getSenderAddress());
		} else {
			logger.error(
					"Message (direct) could not be sent. Message ID = '{}', Target address = '{}', Sender address = '{}'.",
					message.getMessageID(), message.getTargetAddress(), message.getSenderAddress());
		}
		return success;
	}

	/**
	 * Gets and removes a message callback handler
	 * 
	 * @param messageId
	 *            a unique message id
	 * @return a callback handler or <code>null</code> if doesn't exist
	 */
	public IResponseCallBackHandler getCallBackHandler(String messageId) {
		return callBackHandlers.remove(messageId);
	}

	/**
	 * Check if a message callback handler exists.
	 * 
	 * @param messageId
	 *            a unique message id
	 * @return <code>true</code> if exists and not <code>null</code>
	 */
	public boolean checkIfCallbackHandlerExists(String messageId) {
		return callBackHandlers.get(messageId) != null;
	}

	private void prepareMessage(BaseMessage message) {
		message.setSenderAddress(networkManager.getConnection().getPeer().getPeerAddress());
		configureCallbackHandlerIfNeeded(message);
		setSenderPublicKeyIfNeeded(message);
	}

	private RequestP2PConfiguration createSendingConfiguration() {
		return new RequestP2PConfiguration(1, 10, 0);
	}

	private void configureCallbackHandlerIfNeeded(BaseMessage message) {
		if (message instanceof IRequestMessage) {
			IRequestMessage requestMessage = (IRequestMessage) message;
			callBackHandlers.put(message.getMessageID(), requestMessage.getCallBackHandler());
			requestMessage.setCallBackHandler(null);
		}
	}

	/**
	 * Sets the public key of the sender if message is a {@link IRequestMessage} so that the responding node
	 * can easily encrypt the {@link ResponseMessage}.
	 * 
	 * @param message
	 *            message which will be send
	 */
	private void setSenderPublicKeyIfNeeded(BaseMessage message) {
		if (message instanceof IRequestMessage) {
			try {
				message.setSenderPublicKey(networkManager.getSession().getKeyPair().getPublic());
			} catch (NoSessionException e) {
				logger.error("Could not set the sender's public key.");
				message.setSenderPublicKey(null);
			}
		}
	}

	private HybridEncryptedContent signAndEncryptMessage(BaseMessage message, PublicKey targetPublicKey) {
		H2HSession session;
		try {
			session = networkManager.getSession();
		} catch (NoSessionException e) {
			logger.error("No logged in user / no session. The message will not be sent.", e);
			return null;
		}

		byte[] messageBytes;
		HybridEncryptedContent encryptedMessage;
		try {
			// asymmetrically encrypt message
			messageBytes = SerializationUtil.serialize(message);
			encryptedMessage = encryption.encryptHybrid(messageBytes, targetPublicKey);
		} catch (DataLengthException | InvalidKeyException | IllegalStateException | InvalidCipherTextException
				| IllegalBlockSizeException | BadPaddingException | IOException e) {
			logger.error("An exception occured while encrypting the message. The message will not be sent.", e);
			return null;
		}

		try {
			// create signature
			byte[] signature = EncryptionUtil.sign(messageBytes, session.getKeyPair().getPrivate());
			encryptedMessage.setSignature(session.getUserId(), signature);
			return encryptedMessage;
		} catch (InvalidKeyException | SignatureException e) {
			logger.error("An exception occured while signing the message. The message will not be sent.", e);
			return null;
		}
	}

}
