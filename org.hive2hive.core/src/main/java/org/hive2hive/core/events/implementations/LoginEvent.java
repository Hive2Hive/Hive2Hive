package org.hive2hive.core.events.implementations;

import org.hive2hive.core.events.framework.abstracts.UserEvent;
import org.hive2hive.core.events.framework.interfaces.user.ILoginEvent;
import org.hive2hive.core.security.UserCredentials;

public class LoginEvent extends UserEvent implements ILoginEvent {

	public LoginEvent(UserCredentials credentials) {
		super(credentials);
	}

}
