package org.hive2hive.core.events.framework.interfaces;

import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.security.UserCredentials;

public interface IUserEvent {

	UserCredentials getUserCredentials();
	
	void setRollbackReason(RollbackReason reason);
	
	RollbackReason getRollbackReason();
}