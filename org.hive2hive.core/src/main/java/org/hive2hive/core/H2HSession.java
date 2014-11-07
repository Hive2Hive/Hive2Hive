package org.hive2hive.core;

import java.io.File;
import java.nio.file.Path;
import java.security.KeyPair;

import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.file.IFileAgent;
import org.hive2hive.core.model.versioned.Locations;
import org.hive2hive.core.network.data.PublicKeyManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.network.data.download.DownloadManager;
import org.hive2hive.core.network.data.vdht.VersionManager;
import org.hive2hive.core.processes.login.SessionParameters;
import org.hive2hive.core.security.UserCredentials;

public class H2HSession {

	private final UserProfileManager profileManager;
	private final VersionManager<Locations> locationsManager;
	private final PublicKeyManager keyManager;
	private final DownloadManager downloadManager;
	private final IFileConfiguration fileConfiguration;
	private final IFileAgent fileAgent;

	public H2HSession(SessionParameters params) {
		this.profileManager = params.getProfileManager();
		this.locationsManager = params.getLocationsManager();
		this.keyManager = params.getKeyManager();
		this.downloadManager = params.getDownloadManager();
		this.fileConfiguration = params.getFileConfig();
		this.fileAgent = params.getFileAgent();
	}

	public UserProfileManager getProfileManager() {
		return profileManager;
	}

	public VersionManager<Locations> getLocationsManager() {
		return locationsManager;
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

	@Deprecated
	// get rid of 'Path'
	public Path getRoot() {
		return getFileAgent().getRoot().toPath();
	}

	public File getRootFile() {
		return getFileAgent().getRoot();
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

	/**
	 * Returns the download manager, responsible for downloading chunks
	 * 
	 * @return the download manager
	 */
	public DownloadManager getDownloadManager() {
		return downloadManager;
	}

	/**
	 * @return the file agent
	 */
	public IFileAgent getFileAgent() {
		return fileAgent;
	}
}
