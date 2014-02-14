package org.hive2hive.core.processes.implementations.register;

import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideUserProfile;

public class UserProfileCreationStep extends ProcessStep {

	private final String userId;
	private final IProvideUserProfile context;

	public UserProfileCreationStep(String userId, IProvideUserProfile context) {
		this.userId = userId;
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		UserProfile profile = new UserProfile(userId);
		context.provideUserProfile(profile);
	}

}
