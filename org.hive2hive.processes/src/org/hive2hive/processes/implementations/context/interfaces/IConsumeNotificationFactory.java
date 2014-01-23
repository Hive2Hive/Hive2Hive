package org.hive2hive.processes.implementations.context.interfaces;

import java.util.Set;

import org.hive2hive.core.process.notify.BaseNotificationMessageFactory;

public interface IConsumeNotificationFactory {

	BaseNotificationMessageFactory provideMessageFactory();

	Set<String> getUsers();
}
