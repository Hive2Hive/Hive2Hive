package org.hive2hive.core.process.context;

import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.process.common.userprofiletask.GetUserProfileTaskStep;
import org.hive2hive.core.security.HybridEncryptedContent;

/**
 * Simple interface which allows a context to get an user profile task through the
 * {@link GetUserProfileTaskStep} process step.
 * 
 * @author Seppi
 */
public interface IGetUserProfileTaskContext {

	void setUserProfileTask(UserProfileTask userProfileTask);

	UserProfileTask getUserProfileTask();
	
	void setEncryptedUserProfileTask(HybridEncryptedContent encryptedUserProfileTask);
	
	HybridEncryptedContent getEncryptedUserProfileTask();

}
