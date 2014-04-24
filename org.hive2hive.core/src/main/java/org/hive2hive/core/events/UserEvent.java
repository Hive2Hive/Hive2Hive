package org.hive2hive.core.events;

import org.hive2hive.core.events.interfaces.IUserEvent;

public class UserEvent implements IUserEvent {

	private final String userId;
	
	public UserEvent(String userId) {
		this.userId = userId;
	}
	
	@Override
	public String getUserID() {
		return userId;
	}

}