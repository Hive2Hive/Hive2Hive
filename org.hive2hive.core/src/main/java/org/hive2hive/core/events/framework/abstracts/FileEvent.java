package org.hive2hive.core.events.framework.abstracts;

import java.nio.file.Path;

import org.hive2hive.core.events.framework.interfaces.file.IFileEvent;

public abstract class FileEvent implements IFileEvent {

	private Path path;
	private boolean isFile;

	/**
	 * @param path the path
	 * @param isFile indicates whether the path represents a folder or a file. Since the path may
	 *            not exist on disk yet at the time the event occurs, Java may report wrong information
	 *            (if Files#isDirectory or File#isDirectory is used)
	 */
	public FileEvent(Path path, boolean isFile) {
		this.path = path;
		this.isFile = isFile;
	}

	@Override
	public Path getPath() {
		return path;
	}

	@Override
	public boolean isFolder() {
		return !isFile();
	}
	
	@Override
	public boolean isFile() {
		return isFile;
	}
}
