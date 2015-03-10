package org.hive2hive.core.events.framework.interfaces.user;

import org.hive2hive.core.events.framework.IEvent;

public interface IUserEvent extends IEvent {

	/**
	 * @return the currently logged in user
	 */
	String getCurrentUser();
}
