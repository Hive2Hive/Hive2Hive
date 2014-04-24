package org.hive2hive.core.events.framework.interfaces;

import org.hive2hive.core.events.framework.IEventListener;
import org.hive2hive.core.events.framework.interfaces.user.ILoginEvent;
import org.hive2hive.core.events.framework.interfaces.user.ILogoutEvent;
import org.hive2hive.core.events.framework.interfaces.user.IRegisterEvent;

public interface IUserEventListener extends IEventListener {

	void onRegisterSuccess(IRegisterEvent event);

	void onRegisterFailure(IRegisterEvent event);

	void onLoginSuccess(ILoginEvent event);

	void onLoginFailure(ILoginEvent event);

	void onLogoutSuccess(ILogoutEvent event);

	void onLogoutFailure(ILogoutEvent event);

}
