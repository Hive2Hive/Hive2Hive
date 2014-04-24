package org.hive2hive.core.events.implementations;

import org.hive2hive.core.events.framework.abstracts.UserEvent;
import org.hive2hive.core.events.framework.interfaces.user.ILogoutEvent;
import org.hive2hive.core.security.UserCredentials;

public class LogoutEvent extends UserEvent implements ILogoutEvent {

	public LogoutEvent(UserCredentials credentials) {
		super(credentials);
	}

}
