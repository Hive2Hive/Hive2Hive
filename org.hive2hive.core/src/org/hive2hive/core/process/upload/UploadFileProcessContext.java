package org.hive2hive.core.process.upload;

import java.io.File;

import org.hive2hive.core.IH2HFileConfiguration;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.process.ProcessContext;
import org.hive2hive.core.process.common.get.GetUserProfileStep;
import org.hive2hive.core.security.UserCredentials;

public class UploadFileProcessContext extends ProcessContext {

	private final File file;
	private final FileManager fileManager;
	private final IH2HFileConfiguration config;
	private final UserCredentials credentials;
	private UserProfile userProfile;
	private GetUserProfileStep getUserProfileStep;

	public UploadFileProcessContext(UploadFileProcess process, File file, UserCredentials credentials,
			FileManager fileManager, IH2HFileConfiguration config) {
		super(process);
		this.file = file;
		this.credentials = credentials;
		this.fileManager = fileManager;
		this.config = config;
	}

	public File getFile() {
		return file;
	}

	public UserProfile getUserProfile() {
		return userProfile;
	}

	public void setUserProfile(UserProfile userProfile) {
		this.userProfile = userProfile;
	}

	public FileManager getFileManager() {
		return fileManager;
	}

	public IH2HFileConfiguration getConfig() {
		return config;
	}

	public UserCredentials getCredentials() {
		return credentials;
	}

	public GetUserProfileStep getUserProfileStep() {
		return getUserProfileStep;
	}

	public void setUserProfileStep(GetUserProfileStep getUserProfileStep) {
		this.getUserProfileStep = getUserProfileStep;
	}
}
