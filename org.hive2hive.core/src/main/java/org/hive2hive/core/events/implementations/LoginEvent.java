package org.hive2hive.core.events.implementations;

import java.nio.file.Path;

import org.hive2hive.core.events.framework.abstracts.UserEvent;
import org.hive2hive.core.events.framework.interfaces.user.ILoginEvent;
import org.hive2hive.core.security.UserCredentials;

public class LoginEvent extends UserEvent implements ILoginEvent {

	private final Path rootPath;

	public LoginEvent(UserCredentials credentials, Path rootPath) {
		super(credentials);
		this.rootPath = rootPath;
	}

	@Override
	public Path getRootPath() {
		return rootPath;
	}

}
