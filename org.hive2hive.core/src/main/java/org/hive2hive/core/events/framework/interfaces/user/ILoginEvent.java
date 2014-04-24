package org.hive2hive.core.events.framework.interfaces.user;

import java.nio.file.Path;

import org.hive2hive.core.events.framework.interfaces.IUserEvent;

public interface ILoginEvent extends IUserEvent {

	Path getRootPath();
}
