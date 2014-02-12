package org.hive2hive.core.api;

import java.nio.file.Path;

import org.hive2hive.core.api.configs.IFileConfiguration;
import org.hive2hive.core.api.interfaces.IUserManager;
import org.hive2hive.core.exceptions.NoNetworkException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.processes.ProcessFactory;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponent;
import org.hive2hive.core.processes.implementations.login.SessionParameters;
import org.hive2hive.core.security.UserCredentials;

public class H2HUserManager extends NetworkComponent implements IUserManager {

	@Override
	public IProcessComponent register(UserCredentials credentials) throws NoPeerConnectionException,
			NoNetworkException {

		IProcessComponent registerProcess = ProcessFactory.instance().createRegisterProcess(credentials,
				getNetworkManager());

		// node.getProcessManager().submit(registerProcess);
		return registerProcess;
	}

	@Override
	public IProcessComponent login(UserCredentials credentials, IFileConfiguration fileConfig, Path rootPath)
			throws NoPeerConnectionException, NoNetworkException {

		// TODO refactor
		SessionParameters params = new SessionParameters();
		params.setProfileManager(new UserProfileManager(getNetworkManager(), credentials));
		params.setFileManager(new FileManager(rootPath));
		params.setFileConfig(fileConfig);

		IProcessComponent loginProcess = ProcessFactory.instance().createLoginProcess(credentials, params,
				getNetworkManager());

		// node.getProcessManager().submit(loginProcess);
		return loginProcess;
	}

	@Override
	public IProcessComponent logout() throws NoPeerConnectionException, NoSessionException,
			NoNetworkException {

		IProcessComponent logoutProcess = ProcessFactory.instance().createLogoutProcess(getNetworkManager());

		// node.getProcessManager().submit(logoutProcess);
		return logoutProcess;
	}

}
