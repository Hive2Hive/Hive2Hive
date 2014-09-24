package org.hive2hive.core.events.framework.interfaces.file;

import java.nio.file.Path;

public interface IFileMoveEvent extends IFileEvent {
	Path getSrcPath();
	Path getDstPath();
}
