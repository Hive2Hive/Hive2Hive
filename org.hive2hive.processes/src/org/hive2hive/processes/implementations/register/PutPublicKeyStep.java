package org.hive2hive.processes.implementations.register;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.model.UserPublicKey;
import org.hive2hive.processes.framework.RollbackReason;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.processes.implementations.common.BasePutProcessStep;

public class PutPublicKeyStep extends BasePutProcessStep {

	private final UserProfile profile;

	public PutPublicKeyStep(UserProfile profile) {
		this.profile = profile;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {

		UserPublicKey publicKey = new UserPublicKey(profile.getEncryptionKeys()
				.getPublic());

		put(profile.getUserId(), H2HConstants.USER_PUBLIC_KEY, publicKey,
				profile.getProtectionKeys());
	}

	@Override
	protected void doPause() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doResumeExecution() throws InvalidProcessStateException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doResumeRollback() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doRollback(RollbackReason reason) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPutSuccess() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPutFailure() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRemoveSuccess() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRemoveFailure() {
		// TODO Auto-generated method stub

	}

}
