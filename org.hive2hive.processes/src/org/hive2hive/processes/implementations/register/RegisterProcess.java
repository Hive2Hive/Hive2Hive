package org.hive2hive.processes.implementations.register;

import org.hive2hive.core.model.Locations;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.processes.framework.concretes.SequentialProcess;

public final class RegisterProcess extends SequentialProcess {

	public RegisterProcess(UserProfile profile, UserCredentials credentials, RegisterProcessContext context) {
		
		Locations locations = new Locations(profile.getUserId());
		
		add(new GetUserLocationsStep(profile.getUserId(), context, context.getNetworkManager()));
		add(new AssureUserInexistentStep(context));
		add(new PutUserProfileStep(credentials, profile, context.getNetworkManager()));
		add(new PutUserLocationsStep(locations, profile.getProtectionKeys(), context.getNetworkManager()));
		add(new PutPublicKeyStep(profile, context.getNetworkManager()));
	}

}
