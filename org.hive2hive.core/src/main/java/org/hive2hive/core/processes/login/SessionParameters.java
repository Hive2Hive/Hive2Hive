package org.hive2hive.core.processes.login;

import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.file.IFileAgent;
import org.hive2hive.core.model.versioned.Locations;
import org.hive2hive.core.network.data.PublicKeyManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.network.data.download.DownloadManager;
import org.hive2hive.core.network.data.vdht.VersionManager;

public class SessionParameters {

	private final IFileConfiguration fileConfig;
	private final IFileAgent fileAgent;

	private UserProfileManager profileManager;
	private VersionManager<Locations> locationsManager;
	private DownloadManager downloadManager;
	private PublicKeyManager keyManager;

	public SessionParameters(IFileAgent fileAgent, IFileConfiguration fileConfig) {
		this.fileAgent = fileAgent;
		this.fileConfig = fileConfig;
	}

	public UserProfileManager getProfileManager() {
		return profileManager;
	}

	public void setUserProfileManager(UserProfileManager userProfileManager) {
		this.profileManager = userProfileManager;
	}

	public IFileConfiguration getFileConfig() {
		return fileConfig;
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

	public VersionManager<Locations> getLocationsManager() {
		return locationsManager;
	}

	public void setLocationsManager(VersionManager<Locations> locationsManager) {
		this.locationsManager = locationsManager;
	}

	public IFileAgent getFileAgent() {
		return fileAgent;
	}

}
