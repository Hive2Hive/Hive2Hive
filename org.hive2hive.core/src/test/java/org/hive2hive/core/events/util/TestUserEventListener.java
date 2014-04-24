package org.hive2hive.core.events.util;

import org.hive2hive.core.events.framework.interfaces.IUserEventListener;
import org.hive2hive.core.events.framework.interfaces.user.ILoginEvent;
import org.hive2hive.core.events.framework.interfaces.user.ILogoutEvent;
import org.hive2hive.core.events.framework.interfaces.user.IRegisterEvent;

public abstract class TestUserEventListener implements IUserEventListener {

	public boolean registerSuccess = false;
	public boolean registerFailure = false;
	public boolean loginSuccess = false;
	public boolean loginFailure = false;
	public boolean logoutSuccess = false;
	public boolean logoutFailure = false;

	@Override
	public void onRegisterSuccess(IRegisterEvent event) {
		registerSuccess = true;
	}

	@Override
	public void onRegisterFailure(IRegisterEvent event) {
		registerFailure = true;
	}

	@Override
	public void onLoginSuccess(ILoginEvent event) {
		loginSuccess = true;
	}

	@Override
	public void onLoginFailure(ILoginEvent event) {
		loginFailure = true;
	}

	@Override
	public void onLogoutSuccess(ILogoutEvent event) {
		logoutSuccess = true;
	}

	@Override
	public void onLogoutFailure(ILogoutEvent event) {
		logoutFailure = true;
	}

	public void reset() {
		registerSuccess = false;
		registerFailure = false;
		loginSuccess = false;
		loginFailure = false;
		logoutSuccess = false;
		logoutFailure = false;
	}
}
