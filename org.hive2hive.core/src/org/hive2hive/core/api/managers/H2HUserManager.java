package org.hive2hive.core.api.managers;

import java.nio.file.Path;

import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.api.interfaces.IUserManager;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.Locations;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.processes.ProcessFactory;
import org.hive2hive.core.processes.framework.decorators.AsyncComponent;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponent;
import org.hive2hive.core.processes.implementations.common.GetUserLocationsStep;
import org.hive2hive.core.processes.implementations.context.LoginProcessContext;
import org.hive2hive.core.processes.implementations.context.RegisterProcessContext;
import org.hive2hive.core.processes.implementations.login.SessionParameters;
import org.hive2hive.core.security.UserCredentials;

public class H2HUserManager extends H2HManager implements IUserManager {

	private final IFileConfiguration fileConfiguration;

	// TODO remove IFileConfiguration
	public H2HUserManager(NetworkManager networkManager, IFileConfiguration fileConfiguration) {
		super(networkManager);
		this.fileConfiguration = fileConfiguration;
	}

	public H2HUserManager autostart(boolean autostart) {
		configureAutostart(autostart);
		return this;
	}
	
	@Override
	public IProcessComponent register(UserCredentials credentials) throws NoPeerConnectionException {
		IProcessComponent registerProcess = ProcessFactory.instance().createRegisterProcess(credentials,
				networkManager);

		AsyncComponent asyncProcess = new AsyncComponent(registerProcess);

		submitProcess(asyncProcess);
		return asyncProcess;
	}

	@Override
	public IProcessComponent login(UserCredentials credentials, Path rootPath)
			throws NoPeerConnectionException {
		// TODO refactor
		SessionParameters params = new SessionParameters();
		params.setProfileManager(new UserProfileManager(networkManager, credentials));
		params.setRoot(rootPath);
		params.setFileConfig(fileConfiguration);

		IProcessComponent loginProcess = ProcessFactory.instance().createLoginProcess(credentials, params,
				networkManager);

		AsyncComponent asyncProcess = new AsyncComponent(loginProcess);

		submitProcess(asyncProcess);
		return asyncProcess;
	}

	@Override
	public IProcessComponent logout() throws NoPeerConnectionException, NoSessionException {
		IProcessComponent logoutProcess = ProcessFactory.instance().createLogoutProcess(networkManager);

		AsyncComponent asyncProcess = new AsyncComponent(logoutProcess);

		submitProcess(asyncProcess);
		return asyncProcess;
	}
	
	@Override
	public boolean isRegistered(String userId) throws NoPeerConnectionException {
		
		RegisterProcessContext context = new RegisterProcessContext();
		
		// TODO it is better to check for the existence of a user profile
		
		IProcessComponent checkProcess = new GetUserLocationsStep(userId, context, networkManager.getDataManager());
		executeProcess(checkProcess);
		
		return context.consumeLocations() != null;
	}

	@Override
	public boolean isLoggedIn(String userId) throws NoPeerConnectionException {
		
		LoginProcessContext context = new LoginProcessContext();
		
		IProcessComponent checkProcess = new GetUserLocationsStep(userId, context, networkManager.getDataManager());
		executeProcess(checkProcess);
		
		Locations locations = context.consumeLocations();
		
		if (locations == null)
			return false;
		else
			return locations.getPeerAddresses().contains(networkManager.getConnection().getPeer().getPeerAddress());
	}
}
