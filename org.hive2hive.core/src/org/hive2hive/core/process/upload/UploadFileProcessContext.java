package org.hive2hive.core.process.upload;

import org.hive2hive.core.IH2HFileConfiguration;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.process.ProcessContext;
import org.hive2hive.core.process.common.get.GetUserProfileStep;
import org.hive2hive.core.security.UserCredentials;

public class UploadFileProcessContext extends ProcessContext {

	private final FileManager fileManager;
	private final IH2HFileConfiguration config;
	private final UserCredentials credentials;
	private GetUserProfileStep getUserProfileStep;

	public UploadFileProcessContext(UploadFileProcess process, UserCredentials credentials,
			FileManager fileManager, IH2HFileConfiguration config) {
		super(process);
		this.credentials = credentials;
		this.fileManager = fileManager;
		this.config = config;
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
