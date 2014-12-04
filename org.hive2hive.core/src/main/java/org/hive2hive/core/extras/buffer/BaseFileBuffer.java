package org.hive2hive.core.extras.buffer;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.extras.Extra;
import org.hive2hive.core.processes.files.list.FileNode;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Extra
public abstract class BaseFileBuffer implements IFileBuffer {

	private static final Logger logger = LoggerFactory.getLogger(BaseFileBuffer.class);

	protected final IFileManager fileManager;
	protected FileBufferHolder currentBuffer;

	protected BaseFileBuffer(IFileManager fileManager) {
		this.fileManager = fileManager;
	}

	@Override
	public final synchronized void addFileToBuffer(File file) {
		if (currentBuffer == null) {
			currentBuffer = new FileBufferHolder();
			startBuffering(currentBuffer);
		}

		currentBuffer.addFile(file);
	}

	private void startBuffering(final FileBufferHolder fileBuffer) {
		logger.debug("Start buffering for {} ms.", IFileBuffer.BUFFER_WAIT_TIME_MS);
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				currentBuffer = null;
				logger.debug("Finished buffering. {} file(s) in buffer.", fileBuffer.getFileBuffer().size());

				fileBuffer.awaitReady();
				processBuffer(fileBuffer);
			}
		}, BUFFER_WAIT_TIME_MS);

		// start getting the file list
		new Thread(new FileListRunnable(fileBuffer)).start();
	}

	/**
	 * Process the files in the buffer after the buffering time exceeded.
	 * 
	 * @param bufferedFiles
	 */
	protected abstract void processBuffer(IFileBufferHolder buffer);

	private class FileListRunnable implements Runnable {

		private final FileBufferHolder fileBuffer;

		public FileListRunnable(FileBufferHolder fileBuffer) {
			this.fileBuffer = fileBuffer;
		}

		@Override
		public void run() {
			// skip the file list
			if (fileManager == null) {
				fileBuffer.setSyncFiles(null);
				fileBuffer.setReady();
				return;
			}

			IProcessComponent<FileNode> fileList = null;
			try {
				fileList = fileManager.createFileListProcess();
			} catch (NoPeerConnectionException | NoSessionException e) {
				logger.error("Could not get the file list.", e);
				fileBuffer.setSyncFiles(null);
				fileBuffer.setReady();
				return;
			}

			// execute process synchronously
			try {
				fileBuffer.setSyncFiles(fileList.execute());
				fileBuffer.setReady();

			} catch (InvalidProcessStateException ex) {
				logger.error("Could not launch the process to get the file list.", ex);
			} catch (ProcessExecutionException ex) {
				logger.error("Process execution to get the file list failed.", ex);
			}
		}
	}
}
