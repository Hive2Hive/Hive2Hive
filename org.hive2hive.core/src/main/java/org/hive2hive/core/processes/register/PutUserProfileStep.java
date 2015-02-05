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

/**
 * @author Seppi
 */
public class PutUserProfileStep extends BasePutProcessStep {

	private final RegisterProcessContext context;

	public PutUserProfileStep(RegisterProcessContext context, DataManager dataManager) {
		super(dataManager);
		this.setName(getClass().getName());
		this.context = context;
	}

	@Override
	protected Void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		try {
			EncryptedNetworkContent encrypted = dataManager.getEncryption().encryptAES(context.consumeUserProfile(),
					context.consumeUserProfileEncryptionKeys());
			encrypted.generateVersionKey();
			put(context.consumeUserProflieLocationKey(), H2HConstants.USER_PROFILE, encrypted,
					context.consumeUserProfileProtectionKeys());
		} catch (PutFailedException ex) {
			throw new ProcessExecutionException(this, ex);
		} catch (GeneralSecurityException | IllegalStateException | IOException ex) {
			throw new ProcessExecutionException(this, ex, String.format("Cannot encrypt the user profile."));
		}
		return null;
	}

}
