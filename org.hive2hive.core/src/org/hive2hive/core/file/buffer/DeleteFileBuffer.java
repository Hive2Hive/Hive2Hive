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
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponent;
import org.hive2hive.core.processes.implementations.files.list.FileTaste;

public class DeleteFileBuffer extends BaseFileBuffer {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(DeleteFileBuffer.class);
	private static final long MAX_DELETION_PROCESS_DURATION_MS = 30000; // timeout to omit blocks

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
				logger.debug("Starting to delete buffered file " + toDelete);
				IProcessComponent delete = fileManager.delete(toDelete);
				if (!fileManager.isAutostart())
					delete.start();
				delete.await(MAX_DELETION_PROCESS_DURATION_MS);
			} catch (NoSessionException | NoPeerConnectionException | InvalidProcessStateException
					| InterruptedException e) {
				logger.error(e.getMessage());
			}
		}

		logger.debug("Buffer with " + bufferedFiles.size() + " files processed.");
	}
}
