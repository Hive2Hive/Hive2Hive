package org.hive2hive.core.processes.implementations.context.interfaces.common;

import org.hive2hive.core.network.userprofiletask.UserProfileTask;

public interface IUserProfileTaskContext {

	public void provideUserProfileTask(UserProfileTask userProfileTask);

	public UserProfileTask consumeUserProfileTask();

}
