package org.hive2hive.core.network.data;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.UserPublicKey;
import org.hive2hive.core.network.data.parameters.IParameters;
import org.hive2hive.core.network.data.parameters.Parameters;

/**
 * A caching public key manager, which if necessary gets the desired public key of an user from the network.
 * 
 * @author Seppi, Nico
 */
public class PublicKeyManager {

	private final static Logger logger = H2HLoggerFactory.getLogger(PublicKeyManager.class);

	private final String userId;
	private final KeyPair usersKeyPair;
	private final IDataManager dataManager;
	private final Map<String, PublicKey> publicKeyCache;

	public PublicKeyManager(String userId, KeyPair usersKeyPair, IDataManager dataManager) {
		this.userId = userId;
		this.usersKeyPair = usersKeyPair;
		this.dataManager = dataManager;
		this.publicKeyCache = new ConcurrentHashMap<String, PublicKey>();
	}

	/**
	 * Helper method that returns the public key of the currently logged in user.
	 */
	public PublicKey getOwnPublicKey() {
		return usersKeyPair.getPublic();
	}

	/**
	 * Helper method that returns the private key of the currently logged in user
	 */
	public PrivateKey getOwnPrivateKey() {
		return usersKeyPair.getPrivate();
	}

	/**
	 * Helper method that returns the users key pair
	 */
	public KeyPair getOwnKeyPair() {
		return usersKeyPair;
	}

	/**
	 * Returns a copy of all cached public keys
	 */
	public Map<String, PublicKey> getCachedPublicKeys() {
		return Collections.unmodifiableMap(publicKeyCache);
	}

	public void putPublicKey(String userId, PublicKey publicKey) {
		publicKeyCache.put(userId, publicKey);
	}

	public boolean containsPublicKey(String userId) {
		return publicKeyCache.containsKey(userId);
	}

	/**
	 * Gets the public key. If not in cache the method fetches the desired public key from network. In this
	 * case the call blocks.
	 * 
	 * @param userId the unique id of the user
	 * @return the public key of the user
	 * @throws GetFailedException if the public key can't be fetched
	 */
	public PublicKey getPublicKey(String userId) throws GetFailedException {
		logger.debug("Requested to get the public key of user '" + userId + "'.");
		if (this.userId.equals(userId))
			// get the own public key
			return usersKeyPair.getPublic();
		if (publicKeyCache.containsKey(userId))
			// check the cache
			return publicKeyCache.get(userId);

		IParameters parameters = new Parameters().setLocationKey(userId).setContentKey(
				H2HConstants.USER_PUBLIC_KEY);
		NetworkContent content = dataManager.get(parameters);
		return evaluateResult(content, userId);
	}

	private PublicKey evaluateResult(NetworkContent content, String requestingUserId)
			throws GetFailedException {
		if (content == null) {
			logger.warn(String.format("Did not find the public key of user '%s'.", requestingUserId));
			throw new GetFailedException("No public key found.");
		} else if (!(content instanceof UserPublicKey)) {
			logger.error(String
					.format("The received content is not an user public key. Did not find the public key of user '%s'.",
							requestingUserId));
			throw new GetFailedException("Received unkown content.");
		} else {
			logger.trace(String.format("Received sucessfully the public key of user '%s'.", requestingUserId));
			UserPublicKey userPublicKey = (UserPublicKey) content;
			if (userPublicKey.getPublicKey() == null) {
				logger.error(String.format("User public key of user '%s' is corrupted.", requestingUserId));
				throw new GetFailedException("Received corrupted public key.");
			} else {
				logger.debug("Successfully got the public key of user '" + userId + "'.");
				// store it in the cache
				publicKeyCache.put(requestingUserId, userPublicKey.getPublicKey());
				// return it
				return userPublicKey.getPublicKey();
			}
		}
	}

}
