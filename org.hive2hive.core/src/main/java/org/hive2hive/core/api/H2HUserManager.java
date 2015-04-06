package org.hive2hive.core.api;

import java.util.Set;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.api.interfaces.IUserManager;
import org.hive2hive.core.events.framework.interfaces.IUserEventListener;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.IFileAgent;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.processes.ProcessFactory;
import org.hive2hive.core.processes.login.SessionParameters;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.processframework.interfaces.IProcessComponent;

/**
 * Default implementation of {@link IUserManager}.
 * 
 * @author Christian, Nico
 * 
 */
public class H2HUserManager extends H2HManager implements IUserManager {

	public H2HUserManager(NetworkManager networkManager) {
		super(networkManager);
	}

	@Override
	public IProcessComponent<Void> createRegisterProcess(UserCredentials credentials) throws NoPeerConnectionException {
		return ProcessFactory.instance().createRegisterProcess(credentials, networkManager);
	}

	@Override
	public IProcessComponent<Void> createLoginProcess(UserCredentials credentials, IFileAgent fileAgent)
			throws NoPeerConnectionException {
		SessionParameters params = new SessionParameters(fileAgent);
		return ProcessFactory.instance().createLoginProcess(credentials, params, networkManager);
	}

	@Override
	public IProcessComponent<Void> createLogoutProcess() throws NoPeerConnectionException, NoSessionException {
		return ProcessFactory.instance().createLogoutProcess(networkManager);
	}

	@Override
	public boolean isRegistered(String userId) throws NoPeerConnectionException {
		return networkManager.getDataManager().get(
				new Parameters().setLocationKey(userId).setContentKey(H2HConstants.USER_LOCATIONS)) != null;
	}

	@Override
	public boolean isLoggedIn() throws NoPeerConnectionException {
		try {
			return networkManager.getSession() != null;
		} catch (NoSessionException e) {
			return false;
		}
	}

	@Override
	public IProcessComponent<Set<PeerAddress>> createClientsProcess() throws NoSessionException {
		return ProcessFactory.instance().createClientListProcess(networkManager);
	}

	@Override
	public void subscribeFileEvents(IUserEventListener listener) {
		if (listener == null) {
			throw new IllegalArgumentException("The argument listener must not be null.");
		}
		networkManager.getEventBus().subscribe(listener);
	}

}
