package org.hive2hive.core.processes.implementations.context;

import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.processes.implementations.context.interfaces.IUserProfileTaskContext;

public class UserProfileTaskContext implements IUserProfileTaskContext {

	private UserProfileTask profileTask;

	@Override
	public UserProfileTask consumeUserProfileTask() {
		return profileTask;
	}

	@Override
	public void provideUserProfileTask(UserProfileTask profileTask) {
		this.profileTask = profileTask;
	}

}
