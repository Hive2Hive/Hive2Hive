package org.hive2hive.core.events.framework.abstracts;

import java.io.File;

import org.hive2hive.core.events.framework.interfaces.file.IFileEvent;

public abstract class FileEvent implements IFileEvent {

	private final File file;
	private final boolean isFile;

	/**
	 * @param file the file
	 * @param isFile indicates whether the path represents a folder or a file. Since the file may
	 *            not exist on disk yet at the time the event occurs, Java may report wrong information
	 *            (if Files#isDirectory or File#isDirectory is used)
	 */
	public FileEvent(File file, boolean isFile) {
		this.file = file;
		this.isFile = isFile;
	}

	@Override
	public File getFile() {
		return file;
	}

	@Override
	public boolean isFile() {
		return isFile;
	}

	@Override
	public boolean isFolder() {
		return !isFile();
	}
}
