package org.hive2hive.core.process.context;

import org.hive2hive.core.model.UserProfile;

public interface IGetUserProfileContext {

	void setUserProfile(UserProfile userProfile);

	UserProfile getUserProfile();
}
