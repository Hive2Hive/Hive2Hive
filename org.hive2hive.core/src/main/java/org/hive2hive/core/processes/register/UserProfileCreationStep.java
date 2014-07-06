package org.hive2hive.core.processes.register;

import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.processes.context.RegisterProcessContext;
import org.hive2hive.processframework.abstracts.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserProfileCreationStep extends ProcessStep {

	private static final Logger logger = LoggerFactory.getLogger(UserProfileCreationStep.class);

	private final RegisterProcessContext context;

	public UserProfileCreationStep(RegisterProcessContext context) {
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		String userId = context.consumeUserId();
		logger.trace("Creating new user profile. user id ='{}'", userId);
		context.provideUserProfile(new UserProfile(userId));
	}

}
