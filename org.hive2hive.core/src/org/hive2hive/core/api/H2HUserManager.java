package org.hive2hive.core.api;

import java.nio.file.Path;

import org.hive2hive.core.api.configs.IFileConfiguration;
import org.hive2hive.core.api.interfaces.IUserManager;
import org.hive2hive.core.exceptions.NoNetworkException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.processes.ProcessFactory;
import org.hive2hive.core.processes.framework.decorators.AsyncComponent;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponent;
import org.hive2hive.core.processes.implementations.login.SessionParameters;
import org.hive2hive.core.security.UserCredentials;

public class H2HUserManager extends H2HManager implements IUserManager {

	@Override
	public IProcessComponent register(UserCredentials credentials) throws NoNetworkException,
			NoPeerConnectionException {
		IProcessComponent registerProcess = ProcessFactory.instance().createRegisterProcess(credentials,
				getNetworkManager());

		AsyncComponent asyncProcess = new AsyncComponent(registerProcess);
		
		submitProcess(asyncProcess);
		return asyncProcess;
	}

	@Override
	public IProcessComponent login(UserCredentials credentials, IFileConfiguration fileConfig, Path rootPath)
			throws NoNetworkException, NoPeerConnectionException {
		// TODO refactor
		SessionParameters params = new SessionParameters();
		params.setProfileManager(new UserProfileManager(getNetworkManager(), credentials));
		params.setRoot(rootPath);
		params.setFileConfig(fileConfig);

		IProcessComponent loginProcess = ProcessFactory.instance().createLoginProcess(credentials, params,
				getNetworkManager());
		
		AsyncComponent asyncProcess = new AsyncComponent(loginProcess);

		submitProcess(asyncProcess);
		return asyncProcess;
	}

	@Override
	public IProcessComponent logout() throws NoNetworkException, NoPeerConnectionException,
			NoSessionException {
		IProcessComponent logoutProcess = ProcessFactory.instance().createLogoutProcess(getNetworkManager());

		AsyncComponent asyncProcess = new AsyncComponent(logoutProcess);

		submitProcess(asyncProcess);
		return asyncProcess;
	}

}
