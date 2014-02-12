package org.hive2hive.core;

import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyPair;

import org.hive2hive.core.api.configs.IFileConfiguration;
import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.processes.implementations.login.SessionParameters;
import org.hive2hive.core.security.UserCredentials;

public class H2HSession {

	private final UserProfileManager profileManager;
	private final IFileConfiguration fileConfiguration;
	private final KeyPair keyPair;
	private final Path root;

	public H2HSession(SessionParameters params) throws IOException {
		this(params.getKeyPair(), params.getProfileManager(), params.getFileConfig(), params.getRoot());
	}

	// TODO remove constructor
	public H2HSession(KeyPair keyPair, UserProfileManager profileManager,
			IFileConfiguration fileConfiguration, Path root) throws IOException {
		this.keyPair = keyPair;
		this.profileManager = profileManager;
		this.fileConfiguration = fileConfiguration;
		this.root = root;
		if (!root.toFile().exists()) {
			root.toFile().mkdirs();
		}

		FileUtil.writePersistentMetaData(root);
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

	public KeyPair getKeyPair() {
		return keyPair;
	}

	public Path getRoot() {
		return root;
	}
}
