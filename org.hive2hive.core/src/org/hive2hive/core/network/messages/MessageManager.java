package org.hive2hive.core.network.messages;

import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import net.tomp2p.futures.FutureDirect;
import net.tomp2p.futures.FutureResponse;
import net.tomp2p.futures.FutureSend;
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
public final class MessageManager {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(MessageManager.class);

	private final NetworkManager networkManager;
	private final HashMap<String, IResponseCallBackHandler> callBackHandlers;

	public MessageManager(NetworkManager networkManager) {
		this.networkManager = networkManager;
		this.callBackHandlers = new HashMap<String, IResponseCallBackHandler>();
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
		prepareMessage(message);
		message.increaseRoutedSendingCounter();

		// encrypt the message with the given public key
		HybridEncryptedContent encryptedMessage = signAndEncryptMessage(message, targetPublicKey);
		if (encryptedMessage == null)
			return;

		// send message to the peer which is responsible for the given key
		FutureSend futureSend = networkManager.getConnection().getPeer()
				.send(Number160.createHash(message.getTargetKey())).setObject(encryptedMessage)
				.setRequestP2PConfiguration(createSendingConfiguration()).start();
		// attach a future listener to log, handle and notify events
		futureSend.addListener(new FutureRoutedListener(listener, message, targetPublicKey, networkManager));

		logger.debug(String.format("Message sent target key = '%s' message id = '%s'",
				message.getTargetKey(), message.getMessageID()));
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
	public void sendDirect(BaseDirectMessage message, PublicKey targetPublicKey, IBaseMessageListener listener) {
		if (message.getTargetAddress() == null)
			throw new IllegalArgumentException("target address cannot be null");
		if (targetPublicKey == null)
			throw new IllegalArgumentException("target public key cannot be null");

		// prepare message
		prepareMessage(message);
		message.increaseDirectSendingCounter();

		// encrypt the message with the given public key
		HybridEncryptedContent encryptedMessage = signAndEncryptMessage(message, targetPublicKey);
		if (encryptedMessage == null)
			return;

		// send message directly to the peer with the given peer address
		FutureDirect futureDirect = networkManager.getConnection().getPeer()
				.sendDirect(message.getTargetAddress()).setObject(encryptedMessage).start();
		// attach a future listener to log, handle and notify events
		futureDirect
				.addListener(new FutureDirectListener(listener, message, targetPublicKey, networkManager));

		logger.debug(String.format(
				"Message (direct) sent. message id = '%s' target address = '%s' sender address = '%s'",
				message.getMessageID(), message.getTargetAddress(), message.getSenderAddress()));
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
		return (callBackHandlers.get(messageId) != null);
	}

	private void prepareMessage(BaseMessage message) {
		message.setSenderAddress(networkManager.getPeerAddress());
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
			message.setSenderPublicKey(networkManager.getPublicKey());
		}
	}

	private HybridEncryptedContent signAndEncryptMessage(BaseMessage message, PublicKey targetPublicKey) {
		try {
			message.sign(networkManager.getPrivateKey());
		} catch (InvalidKeyException | SignatureException e1) {
			logger.error("An exception occured while signing the message. The message will not be sent.");
			return null;
		}

		// asymmetrically encrypt message
		byte[] messageBytes = EncryptionUtil.serializeObject(message);

		try {
			HybridEncryptedContent encryptedMessage = EncryptionUtil.encryptHybrid(messageBytes,
					targetPublicKey, H2HConstants.HYBRID_AES_KEYLENGTH);

			return encryptedMessage;
		} catch (DataLengthException | InvalidKeyException | IllegalStateException
				| InvalidCipherTextException | IllegalBlockSizeException | BadPaddingException e) {
			logger.error("An exception occured while encrypting the message. The message will not be sent.");
			return null;
		}
	}

}
