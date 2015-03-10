package org.hive2hive.core.events.framework.interfaces;

import net.engio.mbassy.listener.Handler;

import org.hive2hive.core.events.framework.interfaces.user.IUserLoginEvent;
import org.hive2hive.core.events.framework.interfaces.user.IUserLogoutEvent;

public interface IUserEventListener {

	@Handler
	void onClientLogin(IUserLoginEvent loginEvent);

	@Handler
	void onClientLogout(IUserLogoutEvent logoutEvent);
}
