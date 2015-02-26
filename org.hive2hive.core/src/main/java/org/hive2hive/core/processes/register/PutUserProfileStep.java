package org.hive2hive.core.processes.register;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.model.versioned.EncryptedNetworkContent;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.processes.common.base.BasePutProcessStep;
import org.hive2hive.core.processes.context.RegisterProcessContext;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Seppi
 */
public class PutUserProfileStep extends BasePutProcessStep {

	private static final Logger logger = LoggerFactory.getLogger(PutUserProfileStep.class);
	private final RegisterProcessContext context;

	public PutUserProfileStep(RegisterProcessContext context, DataManager dataManager) {
		super(dataManager);
		this.setName(getClass().getName());
		this.context = context;
	}

	@Override
	protected Void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		try {
			logger.debug("Start encrypting the user profile of the new user {}", context.consumeUserId());
			EncryptedNetworkContent encrypted = dataManager.getEncryption().encryptAES(context.consumeUserProfile(),
					context.consumeUserProfileEncryptionKeys());
			encrypted.generateVersionKey();
			logger.debug("User profile successfully encrypted. Start putting it...");
			put(context.consumeUserProflieLocationKey(), H2HConstants.USER_PROFILE, encrypted,
					context.consumeUserProfileProtectionKeys());
			return null;
		} catch (GeneralSecurityException | IllegalStateException | IOException ex) {
			logger.error("Cannot encrypt the user profile of the new user {}", context.consumeUserId(), ex);
			throw new ProcessExecutionException(this, ex, "Cannot encrypt the user profile.");
		} catch (PutFailedException ex) {
			logger.error("Cannot put the user profile of the new user {}", context.consumeUserId(), ex);
			throw new ProcessExecutionException(this, ex);
		}
	}

}
