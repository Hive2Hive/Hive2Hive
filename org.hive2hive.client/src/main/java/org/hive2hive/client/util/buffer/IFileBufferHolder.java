package org.hive2hive.client.util.buffer;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.hive2hive.core.processes.files.list.FileNode;

public interface IFileBufferHolder {

	/**
	 * Get the flat set of files which are in sync with the DHT (use it to filter your files in the buffer)
	 */
	public Set<FileNode> getSyncFiles();

	/**
	 * Get the list of files in the buffer
	 */
	public List<File> getFileBuffer();
}
