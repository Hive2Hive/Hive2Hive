package org.hive2hive.core.events.implementations;

import org.hive2hive.core.events.framework.abstracts.UserEvent;
import org.hive2hive.core.events.framework.interfaces.user.IRegisterEvent;
import org.hive2hive.core.security.UserCredentials;

public class RegisterEvent extends UserEvent implements IRegisterEvent {

	public RegisterEvent(UserCredentials credentials) {
		super(credentials);
	}

}
