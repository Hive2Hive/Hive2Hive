package org.hive2hive.core.processes.context.interfaces;

import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.security.UserCredentials;

public interface IGetUserProfileContext {

	public UserCredentials consumeUserCredentials();

	public void provideUserProfile(UserProfile profile);

}
