package org.hive2hive.core.processes.implementations.userprofiletask;

import java.security.PublicKey;

import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.userprofiletask.TestUserProfileTask;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
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
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		try {
			put(userId, userProfileTask, publicKey);
		} catch (PutFailedException e) {
			throw new ProcessExecutionException(e);
		}
	}
}
