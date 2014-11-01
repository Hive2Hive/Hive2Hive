package org.hive2hive.core.api;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.api.interfaces.IUserManager;
import org.hive2hive.core.events.EventBus;
import org.hive2hive.core.events.framework.interfaces.IUserEventListener;
import org.hive2hive.core.events.framework.interfaces.user.ILoginEvent;
import org.hive2hive.core.events.framework.interfaces.user.ILogoutEvent;
import org.hive2hive.core.events.framework.interfaces.user.IRegisterEvent;
import org.hive2hive.core.events.implementations.LoginEvent;
import org.hive2hive.core.events.implementations.LogoutEvent;
import org.hive2hive.core.events.implementations.RegisterEvent;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.processes.ProcessFactory;
import org.hive2hive.core.processes.login.SessionParameters;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.processframework.RollbackReason;
import org.hive2hive.processframework.abstracts.ProcessComponent;
import org.hive2hive.processframework.decorators.AsyncComponent;
import org.hive2hive.processframework.decorators.CompletionHandleComponent;
import org.hive2hive.processframework.decorators.ICompletionHandle;
import org.hive2hive.processframework.interfaces.IProcessComponent;

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
	public H2HUserManager(NetworkManager networkManager, IFileConfiguration fileConfiguration,
			EventBus eventBus) {
		super(networkManager, eventBus);
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

		CompletionHandleComponent eventComponent = new CompletionHandleComponent(registerProcess,
				createRegisterHandle(credentials));

		AsyncComponent asyncProcess = new AsyncComponent(eventComponent);

		submitProcess(asyncProcess);
		return asyncProcess;
	}

	@Override
	public IProcessComponent login(UserCredentials credentials, Path rootPath) throws NoPeerConnectionException {
		SessionParameters params = new SessionParameters(rootPath, fileConfiguration);

		IProcessComponent loginProcess = ProcessFactory.instance().createLoginProcess(credentials, params, networkManager);

		CompletionHandleComponent eventComponent = new CompletionHandleComponent(loginProcess, createLoginHandle(
				credentials, rootPath));

		AsyncComponent asyncProcess = new AsyncComponent(eventComponent);

		submitProcess(asyncProcess);
		return asyncProcess;
	}

	@Override
	public IProcessComponent logout() throws NoPeerConnectionException, NoSessionException {
		IProcessComponent logoutProcess = ProcessFactory.instance().createLogoutProcess(networkManager);

		CompletionHandleComponent eventComponent = new CompletionHandleComponent(logoutProcess,
				createLogoutHandle(networkManager.getSession().getCredentials()));

		AsyncComponent asyncProcess = new AsyncComponent(eventComponent);

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

	private ICompletionHandle createLoginHandle(UserCredentials credentials, Path rootPath) {

		final ILoginEvent loginEvent = new LoginEvent(credentials, rootPath);

		return new ICompletionHandle() {
			public void onCompletionSuccess() {
				notifyLoginStatus(true, loginEvent);
			}

			public void onCompletionFailure(RollbackReason reason) {
				loginEvent.setRollbackReason(reason);
				notifyLoginStatus(false, loginEvent);
			}
		};
	}

	private ICompletionHandle createLogoutHandle(UserCredentials credentials) {

		final ILogoutEvent logoutEvent = new LogoutEvent(credentials);

		return new ICompletionHandle() {
			public void onCompletionSuccess() {
				notifyLogoutStatus(true, logoutEvent);
			}

			public void onCompletionFailure(RollbackReason reason) {
				logoutEvent.setRollbackReason(reason);
				notifyLogoutStatus(false, logoutEvent);
			}
		};
	}

	private void notifyRegisterStatus(boolean success, IRegisterEvent event) {
		Iterator<IUserEventListener> iterator = eventListeners.iterator();
		while (iterator.hasNext()) {
			if (success) {
				iterator.next().onRegisterSuccess(event);
			} else {
				iterator.next().onRegisterFailure(event);
			}
		}
	}

	private void notifyLoginStatus(boolean success, ILoginEvent event) {
		Iterator<IUserEventListener> iterator = eventListeners.iterator();
		while (iterator.hasNext()) {
			if (success) {
				iterator.next().onLoginSuccess(event);
			} else {
				iterator.next().onLoginFailure(event);
			}
		}
	}

	private void notifyLogoutStatus(boolean success, ILogoutEvent event) {
		Iterator<IUserEventListener> iterator = eventListeners.iterator();
		while (iterator.hasNext()) {
			if (success) {
				iterator.next().onLogoutSuccess(event);
			} else {
				iterator.next().onLogoutFailure(event);
			}
		}
	}
}
