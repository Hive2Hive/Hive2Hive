package org.hive2hive.core.process.register;

import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.process.ProcessContext;
import org.hive2hive.core.security.UserPassword;

public final class RegisterProcessContext extends ProcessContext {

	private final UserPassword userPassword;
	private final UserProfile userProfile;
	
	
	public RegisterProcessContext(RegisterProcess registerProcess, UserPassword password, UserProfile profile) {
		super(registerProcess);
		
		this.userPassword = password;
		this.userProfile = profile;
	}
	
	public UserPassword getUserPassword() {
		return userPassword;
	}

	public UserProfile getUserProfile() {
		return userProfile;
	}
}
