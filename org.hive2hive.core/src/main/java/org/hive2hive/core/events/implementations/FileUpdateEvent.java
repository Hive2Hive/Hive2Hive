package org.hive2hive.core.events.implementations;

import java.io.File;

import org.hive2hive.core.events.framework.abstracts.FileEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileUpdateEvent;

public class FileUpdateEvent extends FileEvent implements IFileUpdateEvent {

	public FileUpdateEvent(File file, boolean isFile) {
		super(file, isFile);
	}

}
