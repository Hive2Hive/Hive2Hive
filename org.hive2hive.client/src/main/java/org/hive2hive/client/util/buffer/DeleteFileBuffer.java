package org.hive2hive.client.util.buffer;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.processes.files.list.FileNode;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteFileBuffer extends BaseFileBuffer {

	private static final Logger logger = LoggerFactory.getLogger(DeleteFileBuffer.class);

	public DeleteFileBuffer(IFileManager fileManager) {
		super(fileManager);
	}

	/**
	 * Process the files in the buffer after the buffering time exceeded.
	 * 
	 * @param buffer the buffer holder
	 */
	protected void processBuffer(IFileBufferHolder buffer) {
		List<File> bufferedFiles = buffer.getFileBuffer();
		Set<FileNode> syncFiles = buffer.getSyncFiles();

		Set<File> toRemove = new HashSet<File>();
		for (File file : bufferedFiles) {
			boolean found = false;
			for (FileNode fileTaste : syncFiles) {
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

		// execute processes synchronously
		IProcessComponent<?> deleteProcess;
		for (File toDelete : bufferedFiles) {
			try {
				deleteProcess = fileManager.createDeleteProcess(toDelete);
			} catch (NoPeerConnectionException | NoSessionException | IllegalArgumentException ex) {
				logger.error("Cannot create a process to delete '{}'.", toDelete.getName(), ex);
				continue;
			}
			try {
				deleteProcess.execute(); // synchronous
			} catch (InvalidProcessStateException ex) {
				logger.error("Cannot start the '{}' to delete '{}'.", deleteProcess, toDelete.getName(), ex);
				continue;
			} catch (ProcessExecutionException ex) {
				logger.error("Process execution of '{}' to delete '{}' failed.", deleteProcess, toDelete.getName(), ex);
				continue;
			}
		}

		logger.debug("Buffer with {} files processed.", bufferedFiles.size());
	}
}
