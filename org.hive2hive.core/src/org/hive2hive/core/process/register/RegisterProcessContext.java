package org.hive2hive.core.process.register;

import org.hive2hive.core.model.UserCredentials;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.process.ProcessContext;

public final class RegisterProcessContext extends ProcessContext {

	private final UserCredentials userCredentials;
	private final UserProfile userProfile;
	
	public RegisterProcessContext(RegisterProcess registerProcess, UserCredentials credentials, UserProfile profile) {
		super(registerProcess);
		
		this.userCredentials = credentials;
		this.userProfile = profile;
	}
	
	public UserCredentials getUserCredentials() {
		return userCredentials;
	}

	public UserProfile getUserProfile() {
		return userProfile;
	}
}
