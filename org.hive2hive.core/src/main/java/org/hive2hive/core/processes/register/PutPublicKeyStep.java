package org.hive2hive.core.processes.register;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.model.UserPublicKey;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.processes.common.base.BasePutProcessStep;
import org.hive2hive.core.processes.context.RegisterProcessContext;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;

public class PutPublicKeyStep extends BasePutProcessStep {

	private final RegisterProcessContext context;

	public PutPublicKeyStep(RegisterProcessContext context, IDataManager dataManager) {
		super(dataManager);
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		UserProfile profile = context.consumeUserProfile();

		UserPublicKey publicKey = new UserPublicKey(profile.getEncryptionKeys().getPublic());
		try {
			put(profile.getUserId(), H2HConstants.USER_PUBLIC_KEY, publicKey, profile.getProtectionKeys());
		} catch (PutFailedException e) {
			throw new ProcessExecutionException(e);
		}
	}

}
