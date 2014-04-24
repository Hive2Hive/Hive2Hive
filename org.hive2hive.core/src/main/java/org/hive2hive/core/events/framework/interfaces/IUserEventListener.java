package org.hive2hive.core.events.framework.interfaces;

import org.hive2hive.core.events.framework.IEventListener;
import org.hive2hive.core.events.framework.interfaces.user.IRegisterEvent;

public interface IUserEventListener extends IEventListener {

	void onRegisterSuccess(IRegisterEvent event);

	void onRegisterFailure(IRegisterEvent event);

	void onLoginSuccess(IUserEvent event);

	void onLoginFailure(IUserEvent event);

	void onLogoutSuccess(IUserEvent event);

	void onLogoutFailure(IUserEvent event);

}
