package org.hive2hive.core.events.framework.abstracts;

import org.hive2hive.core.events.framework.interfaces.IUserEvent;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.processframework.RollbackReason;

public class UserEvent implements IUserEvent {

	private final UserCredentials credentials;
	private RollbackReason rollbackReason = null; // TODO define in super class

	public UserEvent(UserCredentials credentials) {
		this.credentials = credentials;
	}

	@Override
	public UserCredentials getUserCredentials() {
		return credentials;
	}

	@Override
	public void setRollbackReason(RollbackReason reason) {
		rollbackReason = reason;
	}

	@Override
	public RollbackReason getRollbackReason() {
		return rollbackReason;
	}

}