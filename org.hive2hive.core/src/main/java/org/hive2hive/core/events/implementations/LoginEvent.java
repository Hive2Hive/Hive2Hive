package org.hive2hive.core.events.implementations;

import org.hive2hive.core.events.framework.abstracts.UserEvent;
import org.hive2hive.core.events.framework.interfaces.user.ILoginEvent;
import org.hive2hive.core.file.IFileAgent;
import org.hive2hive.core.security.UserCredentials;

public class LoginEvent extends UserEvent implements ILoginEvent {

	private final IFileAgent fileAgent;

	public LoginEvent(UserCredentials credentials, IFileAgent fileAgent) {
		super(credentials);
		this.fileAgent = fileAgent;
	}

	@Override
	public IFileAgent getFileAgent() {
		return fileAgent;
	}
}
