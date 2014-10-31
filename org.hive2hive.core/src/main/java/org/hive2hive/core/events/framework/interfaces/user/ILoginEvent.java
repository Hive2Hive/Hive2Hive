package org.hive2hive.core.events.framework.interfaces.user;

import org.hive2hive.core.events.framework.interfaces.IUserEvent;
import org.hive2hive.core.file.IFileAgent;

public interface ILoginEvent extends IUserEvent {

	IFileAgent getFileAgent();
}
