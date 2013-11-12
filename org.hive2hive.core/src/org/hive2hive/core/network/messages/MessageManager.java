package org.hive2hive.core.network.messages;

import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import net.tomp2p.futures.FutureDirect;
import net.tomp2p.futures.FutureResponse;
import net.tomp2p.p2p.RequestP2PConfiguration;
import net.tomp2p.peers.Number160;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.network.messages.direct.response.IResponseCallBackHandler;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.network.messages.futures.FutureDirectListener;
import org.hive2hive.core.network.messages.futures.FutureRoutedListener;
import org.hive2hive.core.network.messages.request.IRequestMessage;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.HybridEncryptedContent;

/**
 * This class handles the sending of messages.
 * 
 * @author Seppi
 */
public class MessageManager {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(MessageManager.class);

	private final NetworkManager networkManager;
	private final Map<String, IResponseCallBackHandler> callBackHandlers;

	public MessageManager(NetworkManager networkManager) {
		this.networkManager = networkManager;
		this.callBackHandlers = new HashMap<String, IResponseCallBackHandler>();
	}

	/**
	 * Send a message which gets routed to the next responsible node according the
	 * {@link BaseMessage#getTargetKey()} key.</br>
	 * <b>Important:</b> This message gets encrypted with the node's public key. Use this method for direct
	 * sending to nodes, which have the according private key.
	 * 
	 * @param message
	 *            a message to send
	 * @return a future
	 */
	@Deprecated
	public FutureDirect send(BaseMessage message) {
		if (message.getTargetKey() == null)
			throw new IllegalArgumentException("target key cannot be null");
		if (networkManager.getKeyPair() == null)
			throw new IllegalArgumentException("key pair has to be set at network manager");

		// prepare message
		message.increaseSendingCounter();
		message.setSenderAddress(networkManager.getPeerAddress());
		configureCallbackHandlerIfNeeded(message);
		setPublicKeyIfNeeded(message);
		RequestP2PConfiguration requestP2PConfiguration = createSendingConfiguration();

		// encrypt the message with the node's public key
		HybridEncryptedContent encryptedMessage = encryptMessage(message, networkManager.getKeyPair()
				.getPublic());
		if (encryptedMessage == null)
			return null;

		// send message to the peer which is responsible for the given key
		FutureDirect futureDirect = networkManager.getConnection().getPeer()
				.send(Number160.createHash(message.getTargetKey())).setObject(encryptedMessage)
				.setRequestP2PConfiguration(requestP2PConfiguration).start();

		logger.debug(String.format("Message sent target key = '%s' message id = '%s'",
				message.getTargetKey(), message.getMessageID()));

		return futureDirect;
	}

	/**
	 * Send a message which gets routed to the next responsible node according the
	 * {@link BaseMessage#getTargetKey()} key.</br>
	 * <b>Important:</b> This message gets encrypted with the given public key. Use this method for direct
	 * sending to nodes, which have the according private key.</br></br>
	 * <b>Design decision:</b>For an appropriate message handling like resends, error log and notifying
	 * listeners a {@link FutureRoutedListener} future listener gets attached to the {@link FutureDirect}
	 * object.
	 * 
	 * @param message
	 *            a message to send
	 * @param targetPublicKey
	 *            the public key of the receivers node to encrypt the message
	 * @param listener
	 *            a listener which is interested in the result of sending
	 */
	public void send(BaseMessage message, PublicKey targetPublicKey, IBaseMessageListener listener) {
		if (message.getTargetKey() == null)
			throw new IllegalArgumentException("target key cannot be null");
		if (targetPublicKey == null)
			throw new IllegalArgumentException("target public key cannot be null");

		// prepare message
		message.increaseSendingCounter();
		message.setSenderAddress(networkManager.getPeerAddress());
		configureCallbackHandlerIfNeeded(message);
		setPublicKeyIfNeeded(message);
		RequestP2PConfiguration requestP2PConfiguration = createSendingConfiguration();

		// encrypt the message with the given public key
		HybridEncryptedContent encryptedMessage = encryptMessage(message, targetPublicKey);
		if (encryptedMessage == null)
			return;

		// send message to the peer which is responsible for the given key
		FutureDirect futureDirect = networkManager.getConnection().getPeer()
				.send(Number160.createHash(message.getTargetKey())).setObject(encryptedMessage)
				.setRequestP2PConfiguration(requestP2PConfiguration).start();
		// attach a future listener to log, handle and notify events
		futureDirect
				.addListener(new FutureRoutedListener(listener, message, targetPublicKey, networkManager));

		logger.debug(String.format("Message sent target key = '%s' message id = '%s'",
				message.getTargetKey(), message.getMessageID()));
	}

	/**
	 * Send a message directly to a node according the {@link BaseDirectMessage#getTargetAddress()} peer
	 * address.</br>
	 * <b>Important:</b> This message gets encrypted with the node's public key. Use this method for direct
	 * sending to nodes, which have the according private key.
	 * 
	 * @param message
	 *            a direct message to send
	 * @return a future
	 */
	@Deprecated
	public FutureResponse sendDirect(BaseDirectMessage message) {
		if (message.getTargetAddress() == null)
			throw new IllegalArgumentException("target address cannot be null");
		if (networkManager.getKeyPair() == null)
			throw new IllegalArgumentException("key pair has to be set at network manager");

		// prepare message
		message.increaseDirectSendingCounter();
		message.setSenderAddress(networkManager.getPeerAddress());
		setPublicKeyIfNeeded(message);
		configureCallbackHandlerIfNeeded(message);

		// encrypt the message with the node's public key
		HybridEncryptedContent encryptedMessage = encryptMessage(message, networkManager.getKeyPair()
				.getPublic());
		if (encryptedMessage == null)
			return null;

		// send message directly to the peer with the given peer address
		FutureResponse futureResponse = networkManager.getConnection().getPeer()
				.sendDirect(message.getTargetAddress()).setObject(encryptedMessage).start();

		logger.debug(String.format("Message sent (direct) target address = '%s' message id = '%s'",
				message.getTargetAddress(), message.getMessageID()));

		return futureResponse;
	}

	/**
	 * Send a message directly to a node according the {@link BaseDirectMessage#getTargetAddress()} peer
	 * address.</br>
	 * <b>Important:</b> This message gets encrypted with the given public key. Use this method for direct
	 * sending to nodes, which have the according private key.</br></br>
	 * <b>Design decision:</b>For an appropriate message handling like resends, error log and notifying
	 * listeners a {@link FutureDirectListener} future listener gets attached to the {@link FutureResponse}
	 * object.
	 * 
	 * @param message
	 *            a direct message to send
	 * @param targetPublicKey
	 *            the public key of the receivers node to encrypt the message
	 * @param listener
	 *            a listener which is interested in the result of sending
	 */
	public void sendDirect(BaseDirectMessage message, PublicKey targetPublicKey,
			IBaseMessageListener listener) {
		if (message.getTargetAddress() == null)
			throw new IllegalArgumentException("target address cannot be null");
		if (targetPublicKey == null)
			throw new IllegalArgumentException("target public key cannot be null");

		// prepare message
		message.increaseDirectSendingCounter();
		message.setSenderAddress(networkManager.getPeerAddress());
		setPublicKeyIfNeeded(message);
		configureCallbackHandlerIfNeeded(message);

		// encrypt the message with the given public key
		HybridEncryptedContent encryptedMessage = encryptMessage(message, targetPublicKey);
		if (encryptedMessage == null)
			return;

		// send message directly to the peer with the given peer address
		FutureResponse futureResponse = networkManager.getConnection().getPeer()
				.sendDirect(message.getTargetAddress()).setObject(encryptedMessage).start();
		// attach a future listener to log, handle and notify events
		futureResponse.addListener(new FutureDirectListener(listener, message, targetPublicKey,
				networkManager));

		logger.debug(String.format("Message sent (direct) target key = '%s' message id = '%s'",
				message.getTargetKey(), message.getMessageID()));
	}

	public synchronized void addCallBackHandler(String messageId, IResponseCallBackHandler handler) {
		callBackHandlers.put(messageId, handler);
	}

	/**
	 * Gets and removes a message callback handler
	 * 
	 * @param messageId
	 *            a unique message id
	 * @return a callback handler or <code>null</code> if doesn't exist
	 */
	public synchronized IResponseCallBackHandler getCallBackHandler(String messageId) {
		return callBackHandlers.remove(messageId);
	}

	/**
	 * Check if a message callback handler exists.
	 * 
	 * @param messageId
	 *            a unique message id
	 * @return <code>true</code> if exists and not <code>null</code>
	 */
	public synchronized boolean checkIfCallbackHandlerExists(String messageId) {
		return (callBackHandlers.get(messageId) != null);
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
	private void setPublicKeyIfNeeded(BaseMessage message) {
		if (message instanceof IRequestMessage) {
			message.setSenderPublicKey(networkManager.getKeyPair().getPublic());
		}
	}

	private HybridEncryptedContent encryptMessage(BaseMessage message, PublicKey targetPublicKey) {
		// asymmetrically encrypt message
		byte[] messageBytes = EncryptionUtil.serializeObject(message);
		try {
			HybridEncryptedContent encryptedMessage = EncryptionUtil.encryptHybrid(messageBytes,
					targetPublicKey, H2HConstants.H2H_AES_KEYLENGTH);

			return encryptedMessage;
		} catch (DataLengthException | InvalidKeyException | IllegalStateException
				| InvalidCipherTextException | IllegalBlockSizeException | BadPaddingException e) {
			logger.error("An exception occured while encrypting the message. The message will not be sent.");
		}
		return null;
	}

}
