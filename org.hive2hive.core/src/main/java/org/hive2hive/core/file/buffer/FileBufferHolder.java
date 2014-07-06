package org.hive2hive.core.file.buffer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.hive2hive.core.processes.files.list.FileTaste;
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
	private Set<FileTaste> syncFiles;

	public FileBufferHolder() {
		this.fileBuffer = new ArrayList<File>();
		this.syncFilesLatch = new CountDownLatch(1);
	}

	/**
	 * Add a file to the buffer
	 */
	public synchronized void addFile(File file) {
		fileBuffer.add(file);
	}

	/**
	 * Set the files which are in sync with the DHT
	 */
	public void setSyncFiles(Set<FileTaste> syncFiles) {
		this.syncFiles = syncFiles;
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
	public Set<FileTaste> getSyncFiles() {
		return syncFiles;
	}

	@Override
	public List<File> getFileBuffer() {
		return fileBuffer;
	}
}
