package org.hive2hive.core.process.register;

import java.io.IOException;

import javax.crypto.SecretKey;

import org.apache.log4j.Logger;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.common.put.BasePutProcessStep;
import org.hive2hive.core.security.EncryptedNetworkContent;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.PasswordUtil;
import org.hive2hive.core.security.UserCredentials;

/**
 * Generic process step to encrypt the {@link: UserProfile} and add it to the DHT
 * 
 * @author Nico, Seppi
 * 
 */
public class PutUserProfileStep extends BasePutProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(PutUserProfileStep.class);

	protected UserProfile userProfile;
	private final UserCredentials credentials;
	private final ProcessStep nextStep;

	public PutUserProfileStep(UserProfile profile, UserCredentials credentials, ProcessStep nextStep) {
		this.userProfile = profile;
		this.credentials = credentials;
		this.nextStep = nextStep;
	}

	@Override
	public void start() {
		logger.debug("Encrypting UserProfile with 256bit AES key from password");
		try {
			SecretKey encryptionKey = PasswordUtil.generateAESKeyFromPassword(credentials.getPassword(),
					credentials.getPin(), H2HConstants.KEYLENGTH_USER_PROFILE);
			EncryptedNetworkContent encryptedUserProfile = H2HEncryptionUtil.encryptAES(userProfile,
					encryptionKey);
			logger.debug("Putting UserProfile into the DHT");
			encryptedUserProfile.generateVersionKey();
			put(credentials.getProfileLocationKey(), H2HConstants.USER_PROFILE, encryptedUserProfile,
					userProfile.getProtectionKeys());
			getProcess().setNextStep(nextStep);
		} catch (IOException | DataLengthException | IllegalStateException | InvalidCipherTextException e) {
			logger.error("Cannot encrypt the user profile.", e);
			getProcess().stop("User profile could not be encrypted. Reason: " + e.getMessage());
		} catch (PutFailedException e) {
			getProcess().stop(e);
		}
	}
}
