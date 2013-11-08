package org.hive2hive.core.process.login;

import org.hive2hive.core.model.Locations;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.process.ProcessContext;

public class PostLoginProcessContext extends ProcessContext {

	private final UserProfile profile;
	private Locations locations;
	private boolean isDefinedAsMaster = false;
	
	public PostLoginProcessContext(PostLoginProcess postLoginProcess, UserProfile profile, Locations currentLocations) {
		super(postLoginProcess);
		
		this.profile = profile;
		this.locations = currentLocations;
	}
	
	public void setNewLocations(Locations newLocations) {
		this.locations = newLocations;
	}
	
	public void setIsDefinedAsMaster(boolean isDefinedAsMaster) {
		this.isDefinedAsMaster = isDefinedAsMaster;
	}

}
