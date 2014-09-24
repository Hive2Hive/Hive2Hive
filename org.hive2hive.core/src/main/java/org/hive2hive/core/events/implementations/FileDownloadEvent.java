package org.hive2hive.core.events.implementations;

import java.nio.file.Path;

import org.hive2hive.core.events.framework.abstracts.FileEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileDownloadEvent;

public class FileDownloadEvent extends FileEvent implements IFileDownloadEvent {
	public FileDownloadEvent(Path path) {
		super(path);
	}
}
