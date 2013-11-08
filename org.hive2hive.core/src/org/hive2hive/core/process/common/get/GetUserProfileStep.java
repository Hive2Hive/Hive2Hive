package org.hive2hive.core.process.common.get;

import javax.crypto.SecretKey;

import org.apache.log4j.Logger;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.security.EncryptedNetworkContent;
import org.hive2hive.core.security.EncryptionUtil.AES_KEYLENGTH;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.PasswordUtil;
import org.hive2hive.core.security.UserCredentials;

/**
 * Generic process step to get the {@link: UserProfile} and decrypt it. It is then accessible in
 * 
 * @author Nico
 * 
 */
public class GetUserProfileStep extends BaseGetProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(GetUserProfileStep.class);

	private final UserCredentials credentials;
	private final ProcessStep nextStep;
	private UserProfile userProfile;

	public GetUserProfileStep(UserCredentials credentials, ProcessStep nextStep) {
		super(UserProfile.getLocationKey(credentials), H2HConstants.USER_PROFILE);
		
		this.credentials = credentials;
		this.nextStep = nextStep;
	}

	@Override
	protected void handleGetResult(NetworkContent content) {
		if (content == null) {
			// could have been intended...
			logger.warn("Did not find user profile");
		} else {
			EncryptedNetworkContent encrypted = (EncryptedNetworkContent) content;
			logger.debug("Decrpting UserProfile with 256bit AES key from password");

			SecretKey encryptionKey = PasswordUtil
					.generateAESKeyFromPassword(credentials.getPassword(), credentials.getPin(), AES_KEYLENGTH.BIT_256);
			NetworkContent decrypted;
			try {
				decrypted = H2HEncryptionUtil.decryptAES(encrypted, encryptionKey);
				userProfile = (UserProfile) decrypted;
			} catch (DataLengthException | IllegalStateException | InvalidCipherTextException e) {
				logger.error("Cannot decrypt the user profile.", e);
			}
		}

		getProcess().setNextStep(nextStep);
	}

	/**
	 * Returns an object of the user profile. This can be null if the get is not finished yet or it can be
	 * null if the reply did not return anything.
	 * 
	 * @return
	 */
	public UserProfile getUserProfile() {
		return userProfile;
	}
}
