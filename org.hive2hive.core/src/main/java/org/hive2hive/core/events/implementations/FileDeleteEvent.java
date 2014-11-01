package org.hive2hive.core.events.implementations;

import java.io.File;

import org.hive2hive.core.events.framework.abstracts.FileEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileDeleteEvent;

public class FileDeleteEvent extends FileEvent implements IFileDeleteEvent {
	public FileDeleteEvent(File file,  boolean isFile) {
		super(file, isFile);
	}
}
