package org.hive2hive.core.events.util;

import org.hive2hive.core.events.IRegisterEvent;
import org.hive2hive.core.events.interfaces.IUserEvent;
import org.hive2hive.core.events.interfaces.IUserEventListener;

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
	public void onLoginSuccess(IUserEvent event) {
		loginSuccess = true;
	}

	@Override
	public void onLoginFailure(IUserEvent event) {
		loginFailure = true;
	}

	@Override
	public void onLogoutSuccess(IUserEvent event) {
		logoutSuccess = true;
	}

	@Override
	public void onLogoutFailure(IUserEvent event) {
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
