package org.hive2hive.core.processes.register;

import java.io.IOException;

import javax.crypto.SecretKey;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.processes.common.base.BasePutProcessStep;
import org.hive2hive.core.processes.context.RegisterProcessContext;
import org.hive2hive.core.security.EncryptedNetworkContent;
import org.hive2hive.core.security.PasswordUtil;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PutUserProfileStep extends BasePutProcessStep {

	private static final Logger logger = LoggerFactory.getLogger(PutUserProfileStep.class);

	private final RegisterProcessContext context;

	public PutUserProfileStep(RegisterProcessContext context, IDataManager dataManager) {
		super(dataManager);
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		UserCredentials credentials = context.getUserCredentials();

		logger.debug("Starting to encrypt and put the user profile for user '{}'.", credentials.getUserId());

		// consume the profile from the context
		UserProfile userProfile = context.consumeUserProfile();

		// encrypt user profile
		SecretKey encryptionKey = PasswordUtil.generateAESKeyFromPassword(credentials.getPassword(), credentials.getPin(),
				H2HConstants.KEYLENGTH_USER_PROFILE);

		EncryptedNetworkContent encryptedProfile = null;
		try {
			encryptedProfile = dataManager.getEncryption().encryptAES(userProfile, encryptionKey);
		} catch (DataLengthException | IllegalStateException | InvalidCipherTextException | IOException e) {
			throw new ProcessExecutionException("User profile could not be encrypted.");
		}

		try {
			encryptedProfile.generateVersionKey();
		} catch (IOException e) {
			throw new ProcessExecutionException("User profile version key could not be generated.", e);
		}

		// assign ttl value
		encryptedProfile.setTimeToLive(userProfile.getTimeToLive());

		// put encrypted user profile
		try {
			put(credentials.getProfileLocationKey(), H2HConstants.USER_PROFILE, encryptedProfile,
					userProfile.getProtectionKeys());
			logger.debug("User profile successfully put for user {}.", credentials.getUserId());
		} catch (PutFailedException e) {
			throw new ProcessExecutionException(e);
		}
	}
}
