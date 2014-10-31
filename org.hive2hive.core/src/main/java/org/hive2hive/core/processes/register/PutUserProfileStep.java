package org.hive2hive.core.processes.register;

import java.io.IOException;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
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
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		try {
			EncryptedNetworkContent encrypted = dataManager.getEncryption().encryptAES(context.consumeUserProfile(),
					context.consumeUserProfileEncryptionKeys());
			encrypted.generateVersionKey();
			put(context.consumeUserProflieLocationKey(), H2HConstants.USER_PROFILE, encrypted,
					context.consumeUserProfileProtectionKeys());
		} catch (PutFailedException e) {
			throw new ProcessExecutionException(e);
		} catch (DataLengthException | IllegalStateException | InvalidCipherTextException | IOException e) {
			throw new ProcessExecutionException(String.format("Cannot encrypt the user profile. reason = '%s'",
					e.getMessage()));
		}
	}

}
