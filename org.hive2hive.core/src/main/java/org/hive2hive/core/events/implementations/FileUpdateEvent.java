package org.hive2hive.core.events.implementations;

import java.nio.file.Path;

import org.hive2hive.core.events.framework.abstracts.FileEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileUpdateEvent;

public class FileUpdateEvent extends FileEvent implements IFileUpdateEvent {

	public FileUpdateEvent(Path path, boolean isFile) {
		super(path, isFile);
	}

}
