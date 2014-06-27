package org.hive2hive.core.processes.implementations.context.interfaces;

import java.util.Set;

import org.hive2hive.core.model.Index;
import org.hive2hive.core.processes.implementations.files.add.UploadNotificationMessageFactory;

public interface IPrepareNotificationContext {

	public Index consumeIndex();

	public void provideUsersToNotify(Set<String> users);

	public void provideMessageFactory(UploadNotificationMessageFactory messageFactory);

}
