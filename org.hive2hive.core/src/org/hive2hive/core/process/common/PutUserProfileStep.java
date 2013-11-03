package org.hive2hive.core.process.common;

import javax.crypto.SecretKey;

import org.apache.log4j.Logger;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.encryption.EncryptedNetworkContent;
import org.hive2hive.core.encryption.EncryptionUtil.AES_KEYLENGTH;
import org.hive2hive.core.encryption.H2HEncryptionUtil;
import org.hive2hive.core.encryption.PasswordUtil;
import org.hive2hive.core.encryption.UserPassword;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.process.ProcessStep;

/**
 * Generic process step to encrypt the {@link: UserProfile} and add it to the DHT
 * 
 * @author Nico, Seppi
 * 
 */
public class PutUserProfileStep extends PutProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(PutUserProfileStep.class);

	private final UserProfile profile;
	private final UserPassword password;

	public PutUserProfileStep(UserProfile profile, UserPassword password, ProcessStep nextStep) {
		super(profile.getUserId(), H2HConstants.USER_PROFILE, null, nextStep);
		this.profile = profile;
		this.password = password;
	}

	@Override
	public void start() {
		logger.debug("Encrypting UserProfile with 256bit AES key from password");
		try {
			SecretKey encryptionKey = PasswordUtil
					.generateAESKeyFromPassword(password, AES_KEYLENGTH.BIT_256);
			EncryptedNetworkContent encryptedUserProfile = H2HEncryptionUtil.encryptAES(profile,
					encryptionKey);
			logger.debug("Putting UserProfile into the DHT");
			put(locationKey, contentKey, encryptedUserProfile);
		} catch (DataLengthException | IllegalStateException | InvalidCipherTextException e) {
			logger.error("Cannot encrypt the user profile.", e);
			getProcess().rollBack(e.getMessage());
		}
	}

}
