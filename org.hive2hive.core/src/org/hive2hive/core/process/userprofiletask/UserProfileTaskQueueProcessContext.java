package org.hive2hive.core.process.userprofiletask;

import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.context.IGetUserProfileTaskContext;
import org.hive2hive.core.process.context.ProcessContext;
import org.hive2hive.core.security.HybridEncryptedContent;

public class UserProfileTaskQueueProcessContext extends ProcessContext implements IGetUserProfileTaskContext {

	private final UserProfileManager profileManager;
	private final FileManager fileManager;

	private UserProfileTask userProfileTask;
	private HybridEncryptedContent encryptedUserProfileTask;

	public UserProfileTaskQueueProcessContext(Process process, UserProfileManager profileManager,
			FileManager fileManager) {
		super(process);
		this.profileManager = profileManager;
		this.fileManager = fileManager;
	}
	
	public UserProfileManager getProfileManager(){
		return profileManager;
	}
	
	public FileManager getFileManager(){
		return fileManager;
	}

	@Override
	public void setUserProfileTask(UserProfileTask userProfileTask) {
		this.userProfileTask = userProfileTask;
	}

	@Override
	public UserProfileTask getUserProfileTask() {
		return userProfileTask;
	}

	@Override
	public void setEncryptedUserProfileTask(HybridEncryptedContent encryptedUserProfileTask) {
		this.encryptedUserProfileTask = encryptedUserProfileTask;
	}

	@Override
	public HybridEncryptedContent getEncryptedUserProfileTask() {
		return encryptedUserProfileTask;
	}

}
