package org.hive2hive.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyPair;

import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.network.data.PublicKeyManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.processes.implementations.login.SessionParameters;
import org.hive2hive.core.security.UserCredentials;

public class H2HSession {

	private final UserProfileManager profileManager;
	private final PublicKeyManager keyManager;
	private final IFileConfiguration fileConfiguration;
	private final Path root;

	public H2HSession(SessionParameters params) throws IOException {
		this(params.getProfileManager(), params.getKeyManager(), params.getFileConfig(), params.getRoot());
	}

	// TODO remove constructor
	public H2HSession(UserProfileManager profileManager, PublicKeyManager keyManager,
			IFileConfiguration fileConfiguration, Path root) throws IOException {
		this.profileManager = profileManager;
		this.keyManager = keyManager;
		this.fileConfiguration = fileConfiguration;
		this.root = root;
		if (!root.toFile().exists()) {
			root.toFile().mkdirs();
		}
	}

	public UserProfileManager getProfileManager() {
		return profileManager;
	}

	public UserCredentials getCredentials() {
		return profileManager.getUserCredentials();
	}

	public IFileConfiguration getFileConfiguration() {
		return fileConfiguration;
	}

	/**
	 * Returns the own encryption key pair
	 */
	public KeyPair getKeyPair() {
		return keyManager.getOwnKeyPair();
	}

	public Path getRoot() {
		return root;
	}

	public File getRootFile() {
		return root.toFile();
	}

	public String getUserId() {
		return getCredentials().getUserId();
	}

	/**
	 * Get the public key manger to get public keys from other users. A get call may block (if public key not
	 * cached).
	 * 
	 * @return a public key manager
	 */
	public PublicKeyManager getKeyManager() {
		return keyManager;
	}
}
