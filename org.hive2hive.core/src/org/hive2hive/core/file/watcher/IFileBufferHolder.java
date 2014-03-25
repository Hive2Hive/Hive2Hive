package org.hive2hive.core.file.watcher;

import java.io.File;
import java.util.List;
import java.util.Set;

public interface IFileBufferHolder {

	/**
	 * Get the list of files which are in sync with the DHT (use it to filter your files in the buffer)
	 */
	public Set<File> getSyncFiles();

	/**
	 * Get the list of files in the buffer
	 */
	public List<File> getFileBuffer();
}
