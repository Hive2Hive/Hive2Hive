package org.hive2hive.core.process.login;

import org.hive2hive.core.process.ProcessContext;
import org.hive2hive.core.process.common.get.GetLocationsStep;
import org.hive2hive.core.process.common.get.GetUserProfileStep;

public final class LoginProcessContext extends ProcessContext {

	private final GetUserProfileStep profileStep;
	private final GetLocationsStep locationsStep;
	
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
	
}
