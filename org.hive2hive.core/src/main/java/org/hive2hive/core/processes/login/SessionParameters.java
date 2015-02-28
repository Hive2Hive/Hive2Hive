package org.hive2hive.core.processes.login;

import org.hive2hive.core.file.IFileAgent;
import org.hive2hive.core.network.data.PublicKeyManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.network.data.download.DownloadManager;
import org.hive2hive.core.network.data.vdht.LocationsManager;

public class SessionParameters {

	private final IFileAgent fileAgent;

	private UserProfileManager profileManager;
	private LocationsManager locationsManager;
	private DownloadManager downloadManager;
	private PublicKeyManager keyManager;

	public SessionParameters(IFileAgent fileAgent) {
		this.fileAgent = fileAgent;
	}

	public UserProfileManager getProfileManager() {
		return profileManager;
	}

	public void setUserProfileManager(UserProfileManager userProfileManager) {
		this.profileManager = userProfileManager;
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

	public LocationsManager getLocationsManager() {
		return locationsManager;
	}

	public void setLocationsManager(LocationsManager locationsManager) {
		this.locationsManager = locationsManager;
	}

	public IFileAgent getFileAgent() {
		return fileAgent;
	}

}
