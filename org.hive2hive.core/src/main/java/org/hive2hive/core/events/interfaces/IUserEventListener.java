package org.hive2hive.core.events.interfaces;

public interface IUserEventListener extends IEventListener {

	void onRegisterSuccess(IUserEvent event);

	void onRegisterFailure(IUserEvent event);

	void onLoginSuccess(IUserEvent event);

	void onLoginFailure(IUserEvent event);

	void onLogoutSuccess(IUserEvent event);

	void onLogoutFailure(IUserEvent event);

}
