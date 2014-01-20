package org.hive2hive.processes;

import org.hive2hive.core.model.Locations;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.processes.framework.concretes.SequentialProcess;
import org.hive2hive.processes.framework.decorators.AsyncComponent;
import org.hive2hive.processes.framework.interfaces.IProcessComponent;
import org.hive2hive.processes.implementations.register.AssureUserInexistentStep;
import org.hive2hive.processes.implementations.register.GetUserLocationsStep;
import org.hive2hive.processes.implementations.register.PutPublicKeyStep;
import org.hive2hive.processes.implementations.register.PutUserLocationsStep;
import org.hive2hive.processes.implementations.register.PutUserProfileStep;
import org.hive2hive.processes.implementations.register.RegisterProcessContext;


public final class ProcessFactory {

	private static ProcessFactory instance;
	
	public static ProcessFactory instance() {
		if (instance == null)
			instance = new ProcessFactory();
		return instance;
	}
	
	private ProcessFactory() {
	}
	
	public IProcessComponent createRegisterProcess(UserCredentials credentials, UserProfile profile, NetworkManager networkManager) {
		
		RegisterProcessContext context = new RegisterProcessContext(networkManager);
		Locations locations = new Locations(profile.getUserId());
		
		// process composition
		SequentialProcess process = new SequentialProcess();
		
		process.add(new GetUserLocationsStep(profile.getUserId(), context, context.getNetworkManager()));
		process.add(new AssureUserInexistentStep(context));
		process.add(new AsyncComponent(new PutUserProfileStep(credentials, profile, context.getNetworkManager())));
		process.add(new AsyncComponent(new PutUserLocationsStep(locations, profile.getProtectionKeys(), context.getNetworkManager())));
		process.add(new AsyncComponent(new PutPublicKeyStep(profile, context.getNetworkManager())));
		
		AsyncComponent registerProcess = new AsyncComponent(process);
		
		return registerProcess;
	}
}
