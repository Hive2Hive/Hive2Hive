package org.hive2hive.core.process.login;

import java.security.KeyPair;

import org.hive2hive.core.IFileConfiguration;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.network.data.UserProfileManager;

public class SessionParameters {

	private KeyPair keyPair;
	private UserProfileManager profileManager;
	private IFileConfiguration fileConfig;
	private FileManager fileManager;

	public KeyPair getKeyPair() {
		return keyPair;
	}

	public UserProfileManager getProfileManager() {
		return profileManager;
	}

	public IFileConfiguration getFileConfig() {
		return fileConfig;
	}

	public FileManager getFileManager() {
		return fileManager;
	}

	public void setKeyPair(KeyPair keyPair) {
		this.keyPair = keyPair;
	}

	public void setProfileManager(UserProfileManager profileManager) {
		this.profileManager = profileManager;
	}

	public void setFileConfig(IFileConfiguration fileConfig) {
		this.fileConfig = fileConfig;
	}

	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}

}
