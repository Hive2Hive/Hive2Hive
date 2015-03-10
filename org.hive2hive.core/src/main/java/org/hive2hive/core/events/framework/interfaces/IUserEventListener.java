package org.hive2hive.core.events.framework.interfaces;

import net.engio.mbassy.listener.Handler;

import org.hive2hive.core.events.framework.interfaces.user.IUserLoginEvent;

public interface IUserEventListener {

	@Handler
	void onClientLogin(IUserLoginEvent loginEvent);

}
