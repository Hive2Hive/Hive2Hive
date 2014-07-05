package org.hive2hive.core.network.data;

import java.io.IOException;
import java.util.NavigableMap;

import javax.crypto.SecretKey;

import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.model.NetworkContent;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.data.parameters.IParameters;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.security.EncryptedNetworkContent;
import org.hive2hive.core.security.IH2HEncryption;
import org.hive2hive.core.security.PasswordUtil;
import org.hive2hive.core.security.UserCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserProfileHolder {

	private static final Logger logger = LoggerFactory.getLogger(UserProfileHolder.class);
	private final UserCredentials credentials;
	private final DataManager dataManager;
	private final IH2HEncryption encryptionTool;

	// needs to be done only once
	private final SecretKey userProfileEncryptionKey;

	private UserProfile cachedUserProfile = null;

	public UserProfileHolder(UserCredentials credentials, DataManager dataManager) {
		this.credentials = credentials;
		this.dataManager = dataManager;
		this.encryptionTool = dataManager.getEncryption();

		// needs to be done only once
		this.userProfileEncryptionKey = PasswordUtil.generateAESKeyFromPassword(credentials.getPassword(),
				credentials.getPin(), H2HConstants.KEYLENGTH_USER_PROFILE);
	}

	/**
	 * Performs a get call (blocking) and decrypts the received user profile.
	 */
	public void get(QueueEntry entry) {
		logger.debug("Get user profile. user id = '{}'", credentials.getUserId());

		IParameters parameters = new Parameters().setLocationKey(credentials.getProfileLocationKey()).setContentKey(
				H2HConstants.USER_PROFILE);

		// load the current digest list from network
		NavigableMap<Number640, Number160> digest = dataManager.getDigestLatest(parameters);
		// compare the current user profile's version key with the cached one
		if (cachedUserProfile != null && digest != null && digest.firstEntry() != null
				&& digest.firstEntry().getKey().getVersionKey().equals(cachedUserProfile.getVersionKey())) {
			// no need for fetching user profile from network
			entry.setUserProfile(cachedUserProfile);
		} else {
			// load latest user profile from network
			NetworkContent content = dataManager.get(parameters);
			if (content == null) {
				logger.warn("Did not find user profile. user id = '{}'", credentials.getUserId());
				entry.setGetError(new GetFailedException("User profile not found. Got null."));
			} else {
				try {
					logger.trace("Decrypting user profile with 256-bit AES key from password. user id = '{}'",
							credentials.getUserId());
					EncryptedNetworkContent encrypted = (EncryptedNetworkContent) content;
					NetworkContent decrypted = encryptionTool.decryptAES(encrypted, userProfileEncryptionKey);
					UserProfile userProfile = (UserProfile) decrypted;
					userProfile.setVersionKey(content.getVersionKey());
					userProfile.setBasedOnKey(content.getBasedOnKey());

					// cache user profile
					cachedUserProfile = userProfile;
					// provide loaded user profile
					entry.setUserProfile(userProfile);
				} catch (DataLengthException | IllegalStateException | InvalidCipherTextException e) {
					logger.error("Cannot decrypt the user profile.", e);
					entry.setGetError(new GetFailedException(String.format("Cannot decrypt the user profile. reason = '%s'",
							e.getMessage())));
				} catch (Exception e) {
					logger.error("Cannot get the user profile.", e);
					entry.setGetError(new GetFailedException(String.format("Cannot get the user profile. reason = '%s'",
							e.getMessage())));
				}
			}
		}
	}

	/**
	 * Encrypts the modified user profile and puts it (blocking).
	 */
	public void put(PutQueueEntry entry) {
		logger.debug("Put user profile. user id = '{}'", credentials.getUserId());
		try {
			logger.trace("Encrypting user profile with 256bit AES key from password. user id ='{}'", credentials.getUserId());
			EncryptedNetworkContent encryptedUserProfile = encryptionTool.encryptAES(entry.getUserProfile(),
					userProfileEncryptionKey);

			encryptedUserProfile.setBasedOnKey(entry.getUserProfile().getVersionKey());
			encryptedUserProfile.generateVersionKey();

			IParameters parameters = new Parameters().setLocationKey(credentials.getProfileLocationKey())
					.setContentKey(H2HConstants.USER_PROFILE).setVersionKey(encryptedUserProfile.getVersionKey())
					.setData(encryptedUserProfile).setProtectionKeys(entry.getUserProfile().getProtectionKeys())
					.setTTL(entry.getUserProfile().getTimeToLive());

			boolean success = dataManager.put(parameters);
			if (!success) {
				entry.setPutError(new PutFailedException("Put failed."));
			} else {
				// cache user profile
				cachedUserProfile = entry.getUserProfile();
				cachedUserProfile.setBasedOnKey(encryptedUserProfile.getBasedOnKey());
				cachedUserProfile.setVersionKey(encryptedUserProfile.getVersionKey());
			}
		} catch (DataLengthException | IllegalStateException | InvalidCipherTextException | IOException e) {
			logger.error("Cannot encrypt the user profile. reason = '{}'", e.getMessage());
			entry.setPutError(new PutFailedException(String.format("Cannot encrypt the user profile. reason = '%s'",
					e.getMessage())));
		} finally {
			entry.notifyPut();
		}
	}

}
