package org.hive2hive.core.process.upload;

import java.io.File;
import java.security.KeyPair;
import java.util.List;

import org.hive2hive.core.IH2HFileConfiguration;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.context.IGetUserProfileContext;
import org.hive2hive.core.process.context.ProcessContext;
import org.hive2hive.core.security.UserCredentials;

public abstract class BaseUploadFileProcessContext extends ProcessContext implements IGetUserProfileContext {

	private final FileManager fileManager;
	private final IH2HFileConfiguration config;
	private final UserCredentials credentials;
	private final File file;
	private final boolean fileAlreadyExists;
	private List<KeyPair> chunkKeys;
	private UserProfile userProfile;

	protected BaseUploadFileProcessContext(Process process, File file, UserCredentials credentials,
			FileManager fileManager, IH2HFileConfiguration config, boolean fileAlreadyExists) {
		super(process);
		this.file = file;
		this.credentials = credentials;
		this.fileManager = fileManager;
		this.config = config;
		this.fileAlreadyExists = fileAlreadyExists;
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

	public File getFile() {
		return file;
	}

	public boolean getFileAlreadyExists() {
		return fileAlreadyExists;
	}

	public void setChunkKeys(List<KeyPair> chunkKeys) {
		this.chunkKeys = chunkKeys;
	}

	public List<KeyPair> getChunkKeys() {
		return chunkKeys;
	}

	@Override
	public void setUserProfile(UserProfile userProfile) {
		this.userProfile = userProfile;
	}

	@Override
	public UserProfile getUserProfile() {
		return userProfile;
	}

}
