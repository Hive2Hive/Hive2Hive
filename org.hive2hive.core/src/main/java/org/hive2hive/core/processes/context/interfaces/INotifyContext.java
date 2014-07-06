package org.hive2hive.core.processes.context.interfaces;

import java.util.Set;

import org.hive2hive.core.processes.notify.BaseNotificationMessageFactory;

public interface INotifyContext {

	BaseNotificationMessageFactory consumeMessageFactory();

	Set<String> consumeUsersToNotify();
}
