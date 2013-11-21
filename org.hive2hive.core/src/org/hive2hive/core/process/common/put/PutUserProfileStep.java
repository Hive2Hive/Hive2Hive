package org.hive2hive.core.process.common.put;

import javax.crypto.SecretKey;

import org.apache.log4j.Logger;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.UserCredentials;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.security.EncryptedNetworkContent;
import org.hive2hive.core.security.EncryptionUtil.AES_KEYLENGTH;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.PasswordUtil;

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

	public PutUserProfileStep(UserProfile profile, UserCredentials credentials, ProcessStep nextStep) {
		super(nextStep);
		this.userProfile = profile;
		this.credentials = credentials;
	}

	@Override
	public void start() {
		logger.debug("Encrypting UserProfile with 256bit AES key from password");
		try {
			SecretKey encryptionKey = PasswordUtil.generateAESKeyFromPassword(credentials.getPassword(),
					credentials.getPin(), AES_KEYLENGTH.BIT_256);
			EncryptedNetworkContent encryptedUserProfile = H2HEncryptionUtil.encryptAES(userProfile,
					encryptionKey);
			logger.debug("Putting UserProfile into the DHT");
			put(credentials.getProfileLocationKey(), H2HConstants.USER_PROFILE, encryptedUserProfile);
		} catch (DataLengthException | IllegalStateException | InvalidCipherTextException e) {
			logger.error("Cannot encrypt the user profile.", e);
			getProcess().stop(e.getMessage());
		}
	}
}
