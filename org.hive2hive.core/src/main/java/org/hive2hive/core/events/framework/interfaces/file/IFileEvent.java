package org.hive2hive.core.events.framework.interfaces.file;

import java.nio.file.Path;

import org.hive2hive.core.events.framework.IEvent;

public interface IFileEvent extends IEvent {
	Path getPath();
}
