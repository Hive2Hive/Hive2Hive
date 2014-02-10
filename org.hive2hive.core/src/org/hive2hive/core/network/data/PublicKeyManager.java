package org.hive2hive.core.network.data;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.UserPublicKey;

/**
 * A caching public key manager, which if necessary gets the desired public key of an user from the network.
 * 
 * @author Seppi
 */
public class PublicKeyManager {

	private final static Logger logger = H2HLoggerFactory.getLogger(PublicKeyManager.class);

	private final String userId;
	private final KeyPair usersKeyPair;
	private final DataManager dataManager;

	private final Map<String, PublicKey> publicKeys = new HashMap<String, PublicKey>();

	private volatile String requestingUserId;
	private volatile GetFailedException exception;

	public PublicKeyManager(String userId, KeyPair usersKeyPair, DataManager dataManager) {
		this.userId = userId;
		this.usersKeyPair = usersKeyPair;
		this.dataManager = dataManager;
	}

	public PublicKey getUsersPublicKey() {
		return usersKeyPair.getPublic();
	}

	public PrivateKey getUsersPrivateKey() {
		return usersKeyPair.getPrivate();
	}

	/**
	 * Gets the public key. If not in cache the method fetches the desired public key from network. In this
	 * case the call blocks.
	 * 
	 * @param userId the unique id of the user
	 * @return the public key of the user
	 * @throws GetFailedException if the public key can't be fetched
	 */
	public synchronized PublicKey getPublicKey(String userId) throws GetFailedException {
		if (this.userId.equals(userId))
			return usersKeyPair.getPublic();
		if (publicKeys.containsKey(userId)) {
			return publicKeys.get(userId);
		} else {
			exception = null;
			requestingUserId = userId;

			NetworkContent content = dataManager.get(requestingUserId, H2HConstants.USER_PUBLIC_KEY);
			evaluateResult(content);

			if (this.exception != null) {
				throw exception;
			} else {
				return publicKeys.get(requestingUserId);
			}
		}
	}

	private void evaluateResult(NetworkContent content) {
		if (content == null) {
			logger.warn(String.format("Did not find the public key of user '%s'.", requestingUserId));
			exception = new GetFailedException("No public key found.");
		} else if (!(content instanceof UserPublicKey)) {
			logger.error(String
					.format("The received content is not an user public key. Did not find the public key of user '%s'.",
							requestingUserId));
			exception = new GetFailedException("Received unkown content.");
		} else {
			logger.trace(String.format("Received sucessfully the public key of user '%s'.", requestingUserId));
			UserPublicKey userPublicKey = (UserPublicKey) content;
			if (userPublicKey.getPublicKey() == null) {
				logger.error(String.format("User public key of user '%s' is corrupted.", requestingUserId));
				exception = new GetFailedException("Received corrupted public key.");
			} else {
				publicKeys.put(requestingUserId, userPublicKey.getPublicKey());
			}
		}
	}

}
