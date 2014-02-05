package org.hive2hive.core.test.processes.implementations.userprofiletask;

import java.security.PublicKey;

import org.hive2hive.core.exceptions.InvalidProcessStateException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.implementations.common.userprofiletask.PutUserProfileTaskStep;

public class TestPutUserProfileTaskStep extends PutUserProfileTaskStep {

	private final String userId;
	private final TestUserProfileTask userProfileTask;
	private final PublicKey publicKey;

	public TestPutUserProfileTaskStep(String userId, TestUserProfileTask userProfileTask,
			PublicKey publicKey, NetworkManager networkManager) {
		super(networkManager);
		this.userId = userId;
		this.userProfileTask = userProfileTask;
		this.publicKey = publicKey;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		try {
			put(userId, userProfileTask, publicKey);
		} catch (PutFailedException e) {
			cancel(new RollbackReason(this, e.getMessage()));
		}
	}
}
