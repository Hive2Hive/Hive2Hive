package org.hive2hive.core.process.login;

import org.hive2hive.core.model.Locations;
import org.hive2hive.core.process.ProcessContext;
import org.hive2hive.core.process.common.get.GetLocationsStep;
import org.hive2hive.core.process.common.get.GetUserProfileStep;

public final class LoginProcessContext extends ProcessContext {

	private final GetUserProfileStep profileStep;
	private final GetLocationsStep locationsStep;
	
	private Locations locations;
	
	public LoginProcessContext(LoginProcess loginProcess, GetUserProfileStep profileStep, GetLocationsStep locationsStep) {
		super(loginProcess);
		
		this.profileStep = profileStep;
		this.locationsStep = locationsStep;
	}
	
	public GetUserProfileStep getGetUserProfileStep() {
		return profileStep;
	}

	public GetLocationsStep getGetLocationsStep() {
		return locationsStep;
	}
	
	public void setLocations(Locations locations) {
		this.locations = locations;
	}
	
	public Locations getLocations() {
		return locations;
	}
}
