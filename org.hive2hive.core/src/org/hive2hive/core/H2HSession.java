package org.hive2hive.core;

import java.security.KeyPair;

import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.security.UserCredentials;

public class H2HSession {

	private final UserProfileManager profileManager;
	private final IH2HFileConfiguration fileConfiguration;
	private final KeyPair keyPair;
	private final FileManager fileManager;

	public H2HSession(KeyPair keyPair, UserProfileManager profileManager,
			IH2HFileConfiguration fileConfiguration, FileManager fileManager) {
		this.keyPair = keyPair;
		this.profileManager = profileManager;
		this.fileConfiguration = fileConfiguration;
		this.fileManager = fileManager;
	}

	public UserProfileManager getProfileManager() {
		return profileManager;
	}

	public UserCredentials getCredentials() {
		return profileManager.getUserCredentials();
	}

	public IH2HFileConfiguration getFileConfiguration() {
		return fileConfiguration;
	}

	public KeyPair getKeyPair() {
		return keyPair;
	}

	public FileManager getFileManager() {
		return fileManager;
	}
}
