package org.hive2hive.core.process.login;

import org.hive2hive.core.IH2HFileConfiguration;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.process.ProcessContext;
import org.hive2hive.core.security.UserCredentials;

public class PostLoginProcessContext extends ProcessContext {

	private final UserProfile profile;
	private Locations locations;
	private boolean isDefinedAsMaster = false;
	private final FileManager fileManager;
	private final IH2HFileConfiguration fileConfig;
	private final UserCredentials credentials;

	public PostLoginProcessContext(PostLoginProcess postLoginProcess, UserProfile profile,
			UserCredentials credentials, Locations currentLocations, FileManager fileManager,
			IH2HFileConfiguration fileConfig) {
		super(postLoginProcess);
		this.profile = profile;
		this.credentials = credentials;
		this.locations = currentLocations;
		this.fileManager = fileManager;
		this.fileConfig = fileConfig;
	}

	public void setNewLocations(Locations newLocations) {
		this.locations = newLocations;
	}

	public void setIsDefinedAsMaster(boolean isDefinedAsMaster) {
		this.isDefinedAsMaster = isDefinedAsMaster;
	}

	public UserProfile getUserProfile() {
		return profile;
	}

	public Locations getLocations() {
		return locations;
	}

	public boolean isDefinedAsMaster() {
		return isDefinedAsMaster;
	}

	public FileManager getFileManager() {
		return fileManager;
	}

	public IH2HFileConfiguration getFileConfig() {
		return fileConfig;
	}

	public UserCredentials getCredentials() {
		return credentials;
	}
}
