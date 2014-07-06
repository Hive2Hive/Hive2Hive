package org.hive2hive.core.file.buffer;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.processes.files.list.FileTaste;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteFileBuffer extends BaseFileBuffer {

	private static final Logger logger = LoggerFactory.getLogger(DeleteFileBuffer.class);
	// timeout to omit blocks
	private static final long MAX_DELETION_PROCESS_DURATION_MS = 30000;

	public DeleteFileBuffer(IFileManager fileManager) {
		super(fileManager);
	}

	/**
	 * Process the files in the buffer after the buffering time exceeded.
	 * 
	 * @param bufferedFiles
	 */
	protected void processBuffer(IFileBufferHolder buffer) {
		List<File> bufferedFiles = buffer.getFileBuffer();
		Set<FileTaste> syncFiles = buffer.getSyncFiles();

		Set<File> toRemove = new HashSet<File>();
		for (File file : bufferedFiles) {
			boolean found = false;
			for (FileTaste fileTaste : syncFiles) {
				if (fileTaste.getFile().equals(file)) {
					found = true;
					break;
				}
			}

			if (!found) {
				// has already been removed, is not in UP anymore
				toRemove.add(file);
			}
		}
		bufferedFiles.removeAll(toRemove);

		// sort first
		FileUtil.sortPreorder(bufferedFiles);
		// reverse the sorting
		Collections.reverse(bufferedFiles);

		// delete individual files
		for (File toDelete : bufferedFiles) {
			try {
				logger.debug("Starting to delete buffered file {}.", toDelete);
				IProcessComponent delete = fileManager.delete(toDelete);
				if (!fileManager.isAutostart()) {
					delete.start();
				}
				delete.await(MAX_DELETION_PROCESS_DURATION_MS);
			} catch (NoSessionException | NoPeerConnectionException | InvalidProcessStateException | InterruptedException e) {
				logger.error("Cannot start the process to delete {}.", toDelete.getName(), e);
			}
		}

		logger.debug("Buffer with {} files processed.", bufferedFiles.size());
	}
}
