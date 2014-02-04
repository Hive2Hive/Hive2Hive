package org.hive2hive.core.processes.implementations.register;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.model.UserPublicKey;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.implementations.common.base.BasePutProcessStep;

public class PutPublicKeyStep extends BasePutProcessStep {

	private final UserProfile profile;

	public PutPublicKeyStep(UserProfile profile, IDataManager dataManager) {
		super(dataManager);
		this.profile = profile;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		UserPublicKey publicKey = new UserPublicKey(profile.getEncryptionKeys().getPublic());
		try {
			put(profile.getUserId(), H2HConstants.USER_PUBLIC_KEY, publicKey, profile.getProtectionKeys());
		} catch (PutFailedException e) {
			cancel(new RollbackReason(this, e.getMessage()));
		}
	}

}
