package org.hive2hive.core.test.process.common.userprofiletask;

import java.security.PublicKey;

import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.common.userprofiletask.PutUserProfileTaskStep;

public class TestPutUserProfileTaskStep extends PutUserProfileTaskStep {

	private final String userId;
	private final TestUserProfileTask userProfileTask;
	private final PublicKey publicKey;
	private final ProcessStep nextStep;

	public TestPutUserProfileTaskStep(String userId, TestUserProfileTask userProfileTask,
			PublicKey publicKey, ProcessStep nextStep) {
		this.userId = userId;
		this.userProfileTask = userProfileTask;
		this.publicKey = publicKey;
		this.nextStep = nextStep;
	}

	@Override
	public void start() {
		try {
			put(userId, userProfileTask, publicKey);
			getProcess().setNextStep(nextStep);
		} catch (PutFailedException e) {
			getProcess().stop(e);
		}
	}

}
