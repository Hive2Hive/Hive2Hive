package org.hive2hive.processes.implementations.register;

import org.hive2hive.core.model.UserProfile;
import org.hive2hive.processes.framework.concretes.SequentialProcess;

public final class RegisterProcess extends SequentialProcess {

	public RegisterProcess(UserProfile profile) {
		
		// TODO could be passed by constructor
		RegisterProcessContext registerContext = new RegisterProcessContext();
		
		add(new GetUserLocationsStep(profile.getUserId(), registerContext));
		add(new AssureUserInexistentStep(registerContext));
		add(new PutUserProfileStep());
		add(new PutUserLocationsStep());
		add(new PutPublicKeyStep());
	}

}
