package org.hive2hive.core.api;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.api.interfaces.IUserManager;
import org.hive2hive.core.events.IRegisterEvent;
import org.hive2hive.core.events.concretes.RegisterEvent;
import org.hive2hive.core.events.interfaces.IUserEventListener;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.processes.ProcessFactory;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.abstracts.ProcessComponent;
import org.hive2hive.core.processes.framework.decorators.AsyncComponent;
import org.hive2hive.core.processes.framework.decorators.CompletionHandleComponent;
import org.hive2hive.core.processes.framework.decorators.ICompletionHandle;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponent;
import org.hive2hive.core.processes.implementations.common.GetUserLocationsStep;
import org.hive2hive.core.processes.implementations.context.IsRegisteredContext;
import org.hive2hive.core.processes.implementations.login.SessionParameters;
import org.hive2hive.core.security.UserCredentials;

/**
 * Default implementation of {@link IUserManager}.
 * 
 * @author Christian, Nico
 * 
 */
public class H2HUserManager extends H2HManager implements IUserManager {

	private final IFileConfiguration fileConfiguration;

	private List<IUserEventListener> eventListeners;

	// TODO remove IFileConfiguration
	public H2HUserManager(NetworkManager networkManager, IFileConfiguration fileConfiguration) {
		super(networkManager);
		this.fileConfiguration = fileConfiguration;
		eventListeners = new ArrayList<IUserEventListener>();
	}

	public H2HUserManager autostart(boolean autostart) {
		configureAutostart(autostart);
		return this;
	}

	@Override
	public IProcessComponent register(UserCredentials credentials) throws NoPeerConnectionException {
		ProcessComponent registerProcess = ProcessFactory.instance().createRegisterProcess(credentials, networkManager);

		CompletionHandleComponent eventComponent = new CompletionHandleComponent(registerProcess, createRegisterHandle(credentials));

		AsyncComponent asyncProcess = new AsyncComponent(eventComponent);

		submitProcess(asyncProcess);
		return asyncProcess;
	}

	@Override
	public IProcessComponent login(UserCredentials credentials, Path rootPath) throws NoPeerConnectionException {
		// TODO refactor
		SessionParameters params = new SessionParameters();
		params.setProfileManager(new UserProfileManager(networkManager, credentials));
		params.setRoot(rootPath);
		params.setFileConfig(fileConfiguration);

		IProcessComponent loginProcess = ProcessFactory.instance().createLoginProcess(credentials, params, networkManager);

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
		IsRegisteredContext context = new IsRegisteredContext();
		IProcessComponent checkProcess = new GetUserLocationsStep(userId, context, networkManager.getDataManager());
		executeProcess(checkProcess);

		return context.isRegistered();
	}

	@Override
	public boolean isLoggedIn(String userId) throws NoPeerConnectionException {
		try {
			return networkManager.getSession() != null;
		} catch (NoSessionException e) {
			return false;
		}
	}

	@Override
	public synchronized void addEventListener(IUserEventListener listener) {
		eventListeners.add(listener);
	}

	@Override
	public synchronized void removeEventListener(IUserEventListener listener) {
		eventListeners.remove(listener);
	}
	
	private ICompletionHandle createRegisterHandle(UserCredentials credentials) {
		
		final IRegisterEvent registerEvent = new RegisterEvent(credentials);
		
		return new ICompletionHandle() {
			public void onCompletionSuccess() {
				notifyRegisterStatus(true, registerEvent);
			}
			public void onCompletionFailure(RollbackReason reason) {
				registerEvent.setRollbackReason(reason);
				notifyRegisterStatus(false, registerEvent);
			}
		};
	}
	
	private void notifyRegisterStatus(boolean success, IRegisterEvent event) {
		Iterator<IUserEventListener> iterator = eventListeners.iterator();
		while (iterator.hasNext()){
			if (success)
				iterator.next().onRegisterSuccess(event);
			else
				iterator.next().onRegisterFailure(event);
		}
	}
	
}
