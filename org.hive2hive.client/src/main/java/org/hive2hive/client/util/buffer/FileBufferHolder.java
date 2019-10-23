package org.hive2hive.client.util.buffer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.hive2hive.core.processes.files.list.FileNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds two file lists:<br>
 * <ul>
 * <li>The buffered files which have triggered the file observer</li>
 * <li>The files which are in sync / already in the DHT</li>
 * </ul>
 * 
 * @author Nico
 * 
 */
public class FileBufferHolder implements IFileBufferHolder {

	private static final Logger logger = LoggerFactory.getLogger(FileBufferHolder.class);

	// The maximum amount of time to wait until the sync files should be ready.
	private static final long MAX_SYNC_FILES_AWAIT_MS = 20000;

	private final List<File> fileBuffer;
	private final CountDownLatch syncFilesLatch;
	private Set<FileNode> syncFiles;

	public FileBufferHolder() {
		this.fileBuffer = new ArrayList<File>();
		this.syncFilesLatch = new CountDownLatch(1);
	}

	/**
	 * Add a file to the buffer
	 * 
	 * @param file the file to add to the buffer
	 */
	public synchronized void addFile(File file) {
		fileBuffer.add(file);
	}

	/**
	 * Set the files which are in sync with the DHT
	 * 
	 * @param fileNode the base file node to sync recursively
	 */
	public void setSyncFiles(FileNode fileNode) {
		syncFiles = new HashSet<FileNode>();
		addRecursively(fileNode, syncFiles);
	}

	private void addRecursively(FileNode current, Set<FileNode> flatList) {
		if (current == null) {
			return;
		}

		flatList.add(current);

		if (current.isFolder()) {
			for (FileNode child : current.getChildren()) {
				addRecursively(child, flatList);
			}
		}
	}

	/**
	 * Trigger the buffer to be allowed to be processed
	 */
	public void setReady() {
		syncFilesLatch.countDown();
	}

	/**
	 * Wait for the buffer to be ready in a blocking manner
	 */
	public void awaitReady() {
		if (syncFiles == null) {
			try {
				syncFilesLatch.await(MAX_SYNC_FILES_AWAIT_MS, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				logger.error("Could not wait until the file digest was ready.");
			}
		}
	}

	@Override
	public Set<FileNode> getSyncFiles() {
		return syncFiles;
	}

	@Override
	public List<File> getFileBuffer() {
		return fileBuffer;
	}
}
