package org.hive2hive.core.events.abstracts;

import org.hive2hive.core.events.interfaces.IUserEvent;
import org.hive2hive.core.processes.framework.RollbackReason;

public abstract class UserEvent implements IUserEvent {

	private final String userId;
	private RollbackReason rollbackReason = null; // TODO define in super class
	
	public UserEvent(String userId) {
		this.userId = userId;
	}
	
	@Override
	public String getUserID() {
		return userId;
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