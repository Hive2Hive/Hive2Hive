package org.hive2hive.core.events.implementations;

import java.nio.file.Path;

import org.hive2hive.core.events.framework.abstracts.FileEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileDeleteEvent;

public class FileDeleteEvent extends FileEvent implements IFileDeleteEvent {

	public FileDeleteEvent(Path path, boolean isFile) {
		super(path, isFile);
	}

}
