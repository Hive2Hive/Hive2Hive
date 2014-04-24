package org.hive2hive.core.events.concretes;

import org.hive2hive.core.events.IRegisterEvent;
import org.hive2hive.core.events.abstracts.UserEvent;
import org.hive2hive.core.security.UserCredentials;

public class RegisterEvent extends UserEvent implements IRegisterEvent {

	private final UserCredentials credentials;

	public RegisterEvent(UserCredentials credentials) {
		super(credentials.getUserId());
		this.credentials = credentials;
	}

	@Override
	public UserCredentials getUserCredentials() {
		return credentials;
	}

}
