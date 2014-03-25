package org.hive2hive.core.file.watcher;

import java.io.File;
import java.util.List;

import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponent;

public class AddFileBuffer extends BaseFileBuffer {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(AddFileBuffer.class);
	private final IFileManager fileManager;

	public AddFileBuffer(IFileManager fileManager) {
		this.fileManager = fileManager;
	}

	@Override
	protected boolean needsBufferInsertion(File file) {
		// add it to the file buffer if there is not already a parent there; the parent event will trigger
		// the upload of the file
		for (File bufferedFile : fileBuffer) {
			if (file.getAbsolutePath().startsWith(bufferedFile.getAbsolutePath())) {
				logger.debug("Parent (" + bufferedFile.getAbsolutePath()
						+ ") already in buffer, no need to add " + file.getAbsolutePath() + " too.");
				return false;
			}
		}

		// remove all children of this file from the buffer
		for (File bufferedFile : fileBuffer) {
			if (bufferedFile.getAbsolutePath().startsWith(file.getAbsolutePath())) {
				logger.debug("Remove child " + bufferedFile.getAbsolutePath()
						+ " from buffer because parent is now added.");
				fileBuffer.remove(bufferedFile);
			}
		}

		// parent not in buffer --> add to buffer, first in buffer triggers the add file soon
		return true;
	}

	@Override
	protected void processBufferedFiles(List<File> bufferedFiles) {
		for (File toAdd : bufferedFiles) {
			try {
				IProcessComponent process = fileManager.add(toAdd);
				if (!fileManager.isAutostart())
					process.start();
			} catch (NoSessionException | NoPeerConnectionException | IllegalFileLocation
					| InvalidProcessStateException e) {
				logger.error(e.getMessage());
			}
		}
	}

}
