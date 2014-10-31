package org.hive2hive.core.events.implementations;

import java.nio.file.Path;

import org.hive2hive.core.events.framework.abstracts.FileEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileAddEvent;

public class FileAddEvent extends FileEvent implements IFileAddEvent {

	public FileAddEvent(Path path, boolean isFile) {
		super(path, isFile);
	}

}
