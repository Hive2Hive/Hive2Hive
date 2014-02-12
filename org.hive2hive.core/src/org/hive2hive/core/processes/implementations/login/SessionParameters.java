package org.hive2hive.core.processes.implementations.login;

import java.nio.file.Path;
import java.security.KeyPair;

import org.hive2hive.core.api.configs.IFileConfiguration;
import org.hive2hive.core.network.data.UserProfileManager;

public class SessionParameters {

	private KeyPair keyPair;
	private UserProfileManager profileManager;
	private IFileConfiguration fileConfig;
	private Path root;

	public KeyPair getKeyPair() {
		return keyPair;
	}

	public UserProfileManager getProfileManager() {
		return profileManager;
	}

	public IFileConfiguration getFileConfig() {
		return fileConfig;
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

	public void setRoot(Path root) {
		this.root = root;
	}
	
	public Path getRoot() {
		return root;
	}

}
