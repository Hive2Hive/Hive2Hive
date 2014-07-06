package org.hive2hive.core.processes.login;

import java.nio.file.Path;

import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.network.data.PublicKeyManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.network.data.download.DownloadManager;

public class SessionParameters {

	private final UserProfileManager profileManager;
	private final IFileConfiguration fileConfig;
	private final Path root;

	private DownloadManager downloadManager;
	private PublicKeyManager keyManager;

	public SessionParameters(Path root, UserProfileManager profileManager, IFileConfiguration fileConfig) {
		this.root = root;
		this.profileManager = profileManager;
		this.fileConfig = fileConfig;
	}

	public UserProfileManager getProfileManager() {
		return profileManager;
	}

	public IFileConfiguration getFileConfig() {
		return fileConfig;
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

	public DownloadManager getDownloadManager() {
		return downloadManager;
	}

	public void setDownloadManager(DownloadManager downloadManager) {
		this.downloadManager = downloadManager;
	}

}
