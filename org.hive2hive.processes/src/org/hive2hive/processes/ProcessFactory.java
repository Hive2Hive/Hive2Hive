package org.hive2hive.processes;

import org.hive2hive.core.model.Locations;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.login.SessionParameters;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.processes.framework.concretes.SequentialProcess;
import org.hive2hive.processes.framework.decorators.AsyncComponent;
import org.hive2hive.processes.framework.interfaces.IProcessComponent;
import org.hive2hive.processes.implementations.common.GetUserLocationsStep;
import org.hive2hive.processes.implementations.common.PutUserLocationsStep;
import org.hive2hive.processes.implementations.context.LoginProcessContext;
import org.hive2hive.processes.implementations.context.RegisterProcessContext;
import org.hive2hive.processes.implementations.login.ContactOtherClientsStep;
import org.hive2hive.processes.implementations.login.GetUserProfileStep;
import org.hive2hive.processes.implementations.login.SessionCreationStep;
import org.hive2hive.processes.implementations.login.VerifyUserProfileStep;
import org.hive2hive.processes.implementations.register.AssureUserInexistentStep;
import org.hive2hive.processes.implementations.register.PutPublicKeyStep;
import org.hive2hive.processes.implementations.register.PutUserProfileStep;

public final class ProcessFactory {

	private static ProcessFactory instance;

	public static ProcessFactory instance() {
		if (instance == null)
			instance = new ProcessFactory();
		return instance;
	}

	private ProcessFactory() {
	}

	public IProcessComponent createRegisterProcess(UserCredentials credentials,
			UserProfile profile, NetworkManager networkManager) {

		RegisterProcessContext context = new RegisterProcessContext();
		Locations locations = new Locations(profile.getUserId());

		// process composition
		SequentialProcess process = new SequentialProcess();

		process.add(new GetUserLocationsStep(credentials.getUserId(), context,
				networkManager));
		process.add(new AssureUserInexistentStep(context));
		process.add(new AsyncComponent(new PutUserProfileStep(credentials,
				profile, networkManager)));
		process.add(new AsyncComponent(new PutUserLocationsStep(locations,
				profile.getProtectionKeys(), networkManager)));
		process.add(new AsyncComponent(new PutPublicKeyStep(profile,
				networkManager)));

		AsyncComponent registerProcess = new AsyncComponent(process);

		return registerProcess;
	}

	public IProcessComponent createLoginProcess(UserCredentials credentials,
			SessionParameters params, NetworkManager networkManager) {

		LoginProcessContext context = new LoginProcessContext();

		// process composition
		SequentialProcess process = new SequentialProcess();

		process.add(new GetUserProfileStep(credentials, context, networkManager));
		process.add(new VerifyUserProfileStep(credentials.getUserId(), context));
		process.add(new SessionCreationStep(params, context, networkManager));
		process.add(new GetUserLocationsStep(credentials.getUserId(), context,
				networkManager));
		process.add(new ContactOtherClientsStep(context, networkManager));

		AsyncComponent loginProcess = new AsyncComponent(process);

		return loginProcess;
	}
}
