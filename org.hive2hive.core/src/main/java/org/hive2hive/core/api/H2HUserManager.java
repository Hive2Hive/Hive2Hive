package org.hive2hive.core.api;

import java.util.concurrent.Future;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.api.interfaces.IUserManager;
import org.hive2hive.core.events.EventBus;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.IFileAgent;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.processes.ProcessFactory;
import org.hive2hive.core.processes.login.SessionParameters;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.processframework.decorators.AsyncComponent;
import org.hive2hive.processframework.interfaces.IProcessComponent;

/**
 * Default implementation of {@link IUserManager}.
 * This implementation of {@link IUserManager} is asynchronous. Thus, the return types of the
 * {@link IProcessComponent}s uses {@link Future}s.
 * 
 * @author Christian, Nico
 * 
 */
public class H2HUserManager extends H2HManager implements IUserManager {

	private final IFileConfiguration fileConfiguration;

	// TODO remove IFileConfiguration
	public H2HUserManager(NetworkManager networkManager, IFileConfiguration fileConfiguration, EventBus eventBus) {
		super(networkManager, eventBus);
		this.fileConfiguration = fileConfiguration;
	}

	public H2HUserManager autostart(boolean autostart) {
		configureAutostart(autostart);
		return this;
	}

	@Override
	public AsyncComponent<Void> register(UserCredentials credentials) throws NoPeerConnectionException {
		
		IProcessComponent<Void> registerProcess = ProcessFactory.instance().createRegisterProcess(credentials, networkManager);
		AsyncComponent<Void> asyncProcess = new AsyncComponent<>(registerProcess);

		submitProcess(asyncProcess);
		return asyncProcess;
	}

	@Override
	public AsyncComponent<Void> login(UserCredentials credentials, IFileAgent fileAgent) throws NoPeerConnectionException {
		SessionParameters params = new SessionParameters(fileAgent, fileConfiguration);

		IProcessComponent<Void> loginProcess = ProcessFactory.instance().createLoginProcess(credentials, params, networkManager);
		AsyncComponent<Void> asyncProcess = new AsyncComponent<>(loginProcess);

		submitProcess(asyncProcess);
		return asyncProcess;
	}

	@Override
	public AsyncComponent<Void> logout() throws NoPeerConnectionException, NoSessionException {
		
		IProcessComponent<Void> logoutProcess = ProcessFactory.instance().createLogoutProcess(networkManager);
		AsyncComponent<Void> asyncProcess = new AsyncComponent<>(logoutProcess);

		submitProcess(asyncProcess);
		return asyncProcess;
	}

	@Override
	public boolean isRegistered(String userId) throws NoPeerConnectionException {
		return networkManager.getDataManager().get(
				new Parameters().setLocationKey(userId).setContentKey(H2HConstants.USER_LOCATIONS)) != null;
	}

	@Override
	public boolean isLoggedIn(String userId) throws NoPeerConnectionException {
		try {
			return networkManager.getSession() != null;
		} catch (NoSessionException e) {
			return false;
		}
	}

}