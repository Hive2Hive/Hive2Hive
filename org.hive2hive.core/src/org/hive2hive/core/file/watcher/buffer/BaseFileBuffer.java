package org.hive2hive.core.file.watcher.buffer;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.interfaces.IProcessResultListener;
import org.hive2hive.core.processes.framework.interfaces.IResultProcessComponent;
import org.hive2hive.core.processes.implementations.files.list.FileTaste;

public abstract class BaseFileBuffer implements IFileBuffer {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(BaseFileBuffer.class);

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
		logger.debug("Start buffering for " + IFileBuffer.BUFFER_WAIT_TIME_MS + " ms");
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				currentBuffer = null;
				logger.debug("Finished buffering. " + fileBuffer.getFileBuffer().size()
						+ " file(s) in buffer");

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
			IResultProcessComponent<List<FileTaste>> fileList = null;
			try {
				fileList = fileManager.getFileList();
			} catch (NoSessionException e) {
				logger.error("Could not get the file list ", e);
				fileBuffer.setSyncFiles(new HashSet<FileTaste>(0));
				fileBuffer.setReady();
				return;
			}

			fileList.attachListener(new IProcessResultListener<List<FileTaste>>() {
				@Override
				public void onResultReady(List<FileTaste> result) {
					// the result is ready, add it to the buffer
					fileBuffer.setSyncFiles(new HashSet<FileTaste>(result));
					fileBuffer.setReady();
				}
			});

			// start when necessary
			if (!fileManager.isAutostart()) {
				try {
					fileList.start();
				} catch (InvalidProcessStateException e) {
					logger.error("Could not launch the process to get the file list");
				}
			}
		}
	}

}
