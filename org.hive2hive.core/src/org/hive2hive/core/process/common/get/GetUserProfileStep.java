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
import org.hive2hive.core.process.context.IGetUserProfileContext;
import org.hive2hive.core.security.EncryptedNetworkContent;
import org.hive2hive.core.security.EncryptionUtil.AES_KEYLENGTH;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.PasswordUtil;
import org.hive2hive.core.security.UserCredentials;

/**
 * Generic process step to get the {@link: UserProfile} and decrypt it. It is then accessible in
 * 
 * @author Nico, Christian
 * 
 */
public class GetUserProfileStep extends BaseGetProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(GetUserProfileStep.class);

	private final UserCredentials credentials;
	private final ProcessStep nextStep;
	private final IGetUserProfileContext context;

	public GetUserProfileStep(UserCredentials credentials, ProcessStep nextStep,
			IGetUserProfileContext context) {
		this.credentials = credentials;
		this.nextStep = nextStep;
		this.context = context;
	}

	@Override
	public void start() {
		if (context.getUserProfile() == null) {
			get(credentials.getProfileLocationKey(), H2HConstants.USER_PROFILE);
		} else {
			logger.warn("UserProfile is already in context. We do not fetch it again");
			getProcess().setNextStep(nextStep);
		}
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
				context.setUserProfile((UserProfile) decrypted);
			} catch (DataLengthException | IllegalStateException | InvalidCipherTextException e) {
				logger.error("Cannot decrypt the user profile.", e);
			}
		}
		
		getProcess().setNextStep(nextStep);
	}

}
