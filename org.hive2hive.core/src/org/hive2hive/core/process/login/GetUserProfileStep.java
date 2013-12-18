package org.hive2hive.core.process.login;

import javax.crypto.SecretKey;

import org.apache.log4j.Logger;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.common.get.BaseGetProcessStep;
import org.hive2hive.core.process.context.IGetUserProfileContext;
import org.hive2hive.core.security.EncryptedNetworkContent;
import org.hive2hive.core.security.EncryptionUtil.AES_KEYLENGTH;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.PasswordUtil;
import org.hive2hive.core.security.UserCredentials;

/**
 * Generic process step to get the {@link: UserProfile} and decrypt it. Only use it when the
 * {@link UserProfileManager} is not available (yet).
 * 
 * @author Nico, Christian
 * 
 */
public class GetUserProfileStep extends BaseGetProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(GetUserProfileStep.class);

	private final UserCredentials credentials;
	private final ProcessStep nextStep;
	private final IGetUserProfileContext context;

	public GetUserProfileStep(UserCredentials credentials, IGetUserProfileContext context,
			ProcessStep nextStep) {
		this.credentials = credentials;
		this.context = context;
		this.nextStep = nextStep;
	}

	@Override
	public void start() {
		get(credentials.getProfileLocationKey(), H2HConstants.USER_PROFILE);
	}

	@Override
	public void handleGetResult(NetworkContent content) {
		if (content == null) {
			// could have been intended...
			logger.warn("Did not find user profile.");
		} else {
			EncryptedNetworkContent encrypted = (EncryptedNetworkContent) content;
			logger.debug("Decrypting user profile with 256-bit AES key from password.");

			SecretKey encryptionKey = PasswordUtil.generateAESKeyFromPassword(credentials.getPassword(),
					credentials.getPin(), AES_KEYLENGTH.BIT_256);
			try {
				NetworkContent decrypted = H2HEncryptionUtil.decryptAES(encrypted, encryptionKey);
				UserProfile userProfile = (UserProfile) decrypted;
				userProfile.setVersionKey(content.getVersionKey());
				userProfile.setBasedOnKey(content.getBasedOnKey());
				context.setUserProfile(userProfile);
			} catch (DataLengthException | IllegalStateException | InvalidCipherTextException e) {
				logger.error("Cannot decrypt the user profile.", e);
			}
		}

		getProcess().setNextStep(nextStep);
	}
}
