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
	private boolean isElectedMaster = false;
	private final GetUserMessageQueueStep umQueueStep;
	private final FileManager fileManager;
	private final IH2HFileConfiguration fileConfig;
	private final UserCredentials credentials;

	public PostLoginProcessContext(PostLoginProcess postLoginProcess, UserProfile profile,
			UserCredentials credentials, Locations currentLocations, FileManager fileManager,
			IH2HFileConfiguration fileConfig, GetUserMessageQueueStep umQueueStep) {
		super(postLoginProcess);
		this.profile = profile;
		this.credentials = credentials;
		this.locations = currentLocations;
		this.fileManager = fileManager;
		this.fileConfig = fileConfig;
		this.umQueueStep = umQueueStep;
	}

	public void setNewLocations(Locations newLocations) {
		this.locations = newLocations;
	}

	/**
	 * Defines whether client is elected as master client.
	 * 
	 * @param isDefinedAsMaster True, if this client is the master client.
	 */
	public void setIsElectedMaster(boolean isDefinedAsMaster) {
		this.isElectedMaster = isDefinedAsMaster;
	}

	/**
	 * Gets whether this client is elected as master client.
	 * 
	 * @return True, if this client is the master client.
	 */
	public boolean getIsDefinedAsMaster() {
		return isElectedMaster;
	}

	public GetUserMessageQueueStep getGetUserMessageQueueStep() {
		return umQueueStep;
	}

	public UserProfile getUserProfile() {
		return profile;
	}

	public Locations getLocations() {
		return locations;
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
