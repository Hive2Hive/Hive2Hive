package org.hive2hive.core.api;

import java.nio.file.Path;

import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.api.interfaces.IUserManager;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.processes.ProcessFactory;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponent;
import org.hive2hive.core.processes.implementations.login.SessionParameters;
import org.hive2hive.core.security.UserCredentials;

public class UserManager implements IUserManager {

	private final ProcessManager processManager;
	private final NetworkManager networkManager;

	public UserManager(ProcessManager processManager, NetworkManager networkManager) {
		this.processManager = processManager;
		this.networkManager = networkManager;
	}

	@Override
	public IProcessComponent register(UserCredentials credentials) throws NoPeerConnectionException {

		IProcessComponent registerProcess = ProcessFactory.instance().createRegisterProcess(credentials,
				networkManager);
		
		processManager.submit(registerProcess);
		return registerProcess;
	}

	@Override
	public IProcessComponent login(UserCredentials credentials, IFileConfiguration fileConfig, Path rootPath) throws NoPeerConnectionException {

		// TODO refactor
		SessionParameters params = new SessionParameters();
		params.setProfileManager(new UserProfileManager(networkManager, credentials));
		params.setFileManager(new FileManager(rootPath));
		params.setFileConfig(fileConfig);
		
		IProcessComponent loginProcess = ProcessFactory.instance().createLoginProcess(credentials, params, networkManager);
		
		processManager.submit(loginProcess);
		return loginProcess;
	}

	@Override
	public IProcessComponent logout() throws NoPeerConnectionException, NoSessionException {
		
		IProcessComponent logoutProcess = ProcessFactory.instance().createLogoutProcess(networkManager);
		
		processManager.submit(logoutProcess);
		return logoutProcess;
	}

}
