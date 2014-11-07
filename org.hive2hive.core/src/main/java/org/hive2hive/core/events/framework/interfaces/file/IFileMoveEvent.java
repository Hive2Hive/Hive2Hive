package org.hive2hive.core.events.framework.interfaces.file;

import java.io.File;

public interface IFileMoveEvent extends IFileEvent {
	File getSrcFile();

	File getDstFile();
}
