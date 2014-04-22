package org.hive2hive.core.processes.implementations.context;

import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeUserProfileTask;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideUserProfileTask;

public class UserProfileTaskContext implements IProvideUserProfileTask, IConsumeUserProfileTask {

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
