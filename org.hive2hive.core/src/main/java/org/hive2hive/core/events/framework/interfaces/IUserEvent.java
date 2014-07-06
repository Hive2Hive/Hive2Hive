package org.hive2hive.core.events.framework.interfaces;

import org.hive2hive.core.events.framework.IEvent;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.processframework.RollbackReason;

public interface IUserEvent extends IEvent {

	UserCredentials getUserCredentials();
	
	void setRollbackReason(RollbackReason reason);
	
	RollbackReason getRollbackReason();
}