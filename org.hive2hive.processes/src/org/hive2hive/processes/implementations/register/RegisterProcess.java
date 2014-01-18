package org.hive2hive.processes.implementations.register;

import org.hive2hive.core.model.Locations;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.processes.framework.concretes.SequentialProcess;

public final class RegisterProcess extends SequentialProcess {

	public RegisterProcess(UserCredentials credentials) {
		
		// TODO could be passed by constructor
		RegisterProcessContext registerContext = new RegisterProcessContext();
		
		UserProfile profile = new UserProfile(credentials.getUserId());
		Locations locations = new Locations(profile.getUserId());
		
		add(new GetUserLocationsStep(profile.getUserId(), registerContext));
		add(new AssureUserInexistentStep(registerContext));
		add(new PutUserProfileStep(credentials, profile));
		add(new PutUserLocationsStep(locations, profile.getProtectionKeys()));
		add(new PutPublicKeyStep(profile));
	}

}
