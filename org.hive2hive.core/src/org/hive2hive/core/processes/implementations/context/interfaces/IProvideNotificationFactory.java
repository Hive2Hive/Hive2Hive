package org.hive2hive.core.processes.implementations.context.interfaces;

import java.util.Set;

import org.hive2hive.core.process.notify.BaseNotificationMessageFactory;

public interface IProvideNotificationFactory {

	void provideMessageFactory(BaseNotificationMessageFactory messageFactory);

	void provideUsersToNotify(Set<String> users);
}
