package org.hive2hive.core.processes.register;

import java.security.KeyPair;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.processes.context.RegisterProcessContext;
import org.hive2hive.core.security.IH2HEncryption;
import org.hive2hive.processframework.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Seppi
 */
public class UserProfileCreationStep extends ProcessStep<Void> {

	private static final Logger logger = LoggerFactory.getLogger(UserProfileCreationStep.class);

	private final RegisterProcessContext context;
	private final IH2HEncryption encryption;

	public UserProfileCreationStep(RegisterProcessContext context, IH2HEncryption encryption) {
		this.encryption = encryption;
		this.setName(getClass().getName());
		this.context = context;
	}

	@Override
	protected Void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		String userId = context.consumeUserId();
		logger.trace("Creating new user profile. user id ='{}'", userId);

		// generate keys
		KeyPair encryptionKeys = encryption.generateRSAKeyPair(H2HConstants.KEYLENGTH_USER_KEYS);
		KeyPair protectionKeys = encryption.generateRSAKeyPair(H2HConstants.KEYLENGTH_PROTECTION);

		context.provideUserProfile(new UserProfile(userId, encryptionKeys, protectionKeys));
		return null;
	}

}
