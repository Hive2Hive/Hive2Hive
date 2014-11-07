package org.hive2hive.core.processes.register;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.model.UserPublicKey;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.processes.common.base.BasePutProcessStep;
import org.hive2hive.core.processes.context.RegisterProcessContext;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;

/**
 * @author Seppi
 */
public class PutPublicKeyStep extends BasePutProcessStep {

	private final RegisterProcessContext context;

	public PutPublicKeyStep(RegisterProcessContext context, DataManager dataManager) {
		super(dataManager);
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		UserPublicKey publicKey = new UserPublicKey(context.consumeUserProfile().getEncryptionKeys().getPublic());
		try {
			put(context.consumeUserId(), H2HConstants.USER_PUBLIC_KEY, publicKey,
					context.consumeUserPublicKeyProtectionKeys());
		} catch (PutFailedException e) {
			throw new ProcessExecutionException(e);
		}
	}

}
