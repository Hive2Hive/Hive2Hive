package org.hive2hive.core.events.interfaces;

import org.hive2hive.core.processes.framework.RollbackReason;

public interface IUserEvent {

	String getUserID();
	
	void setRollbackReason(RollbackReason reason);
	
	RollbackReason getRollbackReason();
}