package org.hive2hive.core.events.framework.interfaces.file;

import java.io.File;

import org.hive2hive.core.events.framework.IEvent;

public interface IFileEvent extends IEvent {
	File getFile();

	boolean isFile();

	boolean isFolder();
}
