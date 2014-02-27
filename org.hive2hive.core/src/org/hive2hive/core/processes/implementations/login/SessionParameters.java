package org.hive2hive.core.processes.implementations.login;

import java.nio.file.Path;

import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.network.data.PublicKeyManager;
import org.hive2hive.core.network.data.UserProfileManager;

public class SessionParameters {

	private UserProfileManager profileManager;
	private PublicKeyManager keyManager;
	private IFileConfiguration fileConfig;
	private Path root;

	public UserProfileManager getProfileManager() {
		return profileManager;
	}

	public IFileConfiguration getFileConfig() {
		return fileConfig;
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

	public PublicKeyManager getKeyManager() {
		return keyManager;
	}

	public void setKeyManager(PublicKeyManager keyManager) {
		this.keyManager = keyManager;
	}

}
