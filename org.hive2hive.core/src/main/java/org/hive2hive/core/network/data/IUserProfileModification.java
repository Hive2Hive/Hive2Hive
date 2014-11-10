package org.hive2hive.core.network.data;

import org.hive2hive.core.exceptions.AbortModifyException;
import org.hive2hive.core.model.versioned.UserProfile;

public interface IUserProfileModification {

	/**
	 * Modify the user profile in this method. If a version fork occurs, this method could be called multiple
	 * times, thus make sure that slow operations like key generation happen before.
	 * 
	 * @param userProfile the user profile
	 * @throws AbortModifyException if the modification fails and the Userprofile should not be put
	 */
	public void modifyUserProfile(UserProfile userProfile) throws AbortModifyException;
}
