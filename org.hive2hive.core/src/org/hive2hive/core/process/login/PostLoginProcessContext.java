package org.hive2hive.core.process.login;

import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.process.ProcessContext;

public class PostLoginProcessContext extends ProcessContext {

	private final UserProfile profile;
	private Locations locations;
	private boolean isDefinedAsMaster = false;
	private final FileManager fileManager;

	public PostLoginProcessContext(PostLoginProcess postLoginProcess, UserProfile profile,
			Locations currentLocations, FileManager fileManager) {
		super(postLoginProcess);
		this.profile = profile;
		this.locations = currentLocations;
		this.fileManager = fileManager;
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
}
