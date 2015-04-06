package org.hive2hive.core.events.framework.abstracts;

import org.hive2hive.core.events.framework.interfaces.user.IUserEvent;

public class UserEvent implements IUserEvent {

	private final String currentUser;

	public UserEvent(String currentUser) {
		this.currentUser = currentUser;
	}

	@Override
	public String getCurrentUser() {
		return currentUser;
	}

}
