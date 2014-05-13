package org.hive2hive.core.processes.implementations.context.interfaces.common;

import java.util.Set;

import org.hive2hive.core.processes.implementations.notify.BaseNotificationMessageFactory;

public interface IConsumeNotificationFactory {

	BaseNotificationMessageFactory consumeMessageFactory();

	Set<String> consumeUsersToNotify();
}
