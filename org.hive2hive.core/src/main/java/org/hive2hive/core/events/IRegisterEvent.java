package org.hive2hive.core.events;

import org.hive2hive.core.events.interfaces.IUserEvent;
import org.hive2hive.core.security.UserCredentials;

public interface IRegisterEvent extends IUserEvent {

	UserCredentials getUserCredentials();
}
