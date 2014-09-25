package org.hive2hive.core.events.framework.abstracts;

import java.nio.file.Path;

import org.hive2hive.core.events.framework.interfaces.file.IFileEvent;

public class FileEvent implements IFileEvent {

	private Path path;

	public FileEvent(Path path) {
		this.path = path;
	}

	@Override
	public Path getPath() {
		return path;
	}
}
