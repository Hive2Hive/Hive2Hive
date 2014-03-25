package org.hive2hive.core.file.watcher;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponent;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponentListener;

public class DeleteFileBuffer extends BaseFileBuffer {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(DeleteFileBuffer.class);
	private static final long MAX_DELETION_PROCESS_DURATION_MS = 30000; // timeout to omit blocks

	private final IFileManager fileManager;

	public DeleteFileBuffer(IFileManager fileManager) {
		this.fileManager = fileManager;
	}

	@Override
	protected boolean needsBufferInsertion(File file) {
		// add all files to the buffer
		return true;
	}

	/**
	 * Process the files in the buffer after the buffering time exceeded.
	 * 
	 * @param bufferedFiles
	 */
	protected void processBufferedFiles(List<File> bufferedFiles) {
		// sort first
		FileUtil.sortPreorder(bufferedFiles);
		// reverse the sorting
		Collections.reverse(bufferedFiles);

		// delete individual files
		for (File toDelete : bufferedFiles) {
			try {
				logger.debug("Starting to delete buffered file " + toDelete);
				IProcessComponent delete = fileManager.delete(toDelete);
				BlockingListener listener = new BlockingListener();
				delete.attachListener(listener);
				if (!fileManager.isAutostart())
					delete.start();

				// wait for execution
				listener.waitForFinish();
			} catch (NoSessionException | NoPeerConnectionException | InvalidProcessStateException e) {
				logger.error(e.getMessage());
			}
		}

		logger.debug("Buffer with " + bufferedFiles.size() + " files processed.");
	}

	/**
	 * Listener blocking until the process is finished
	 */
	private class BlockingListener implements IProcessComponentListener {

		private final CountDownLatch latch;

		public BlockingListener() {
			this.latch = new CountDownLatch(1);
		}

		@Override
		public void onSucceeded() {
			latch.countDown();
		}

		@Override
		public void onFailed(RollbackReason reason) {
			latch.countDown();
		}

		@Override
		public void onFinished() {
			latch.countDown();
		}

		public void waitForFinish() {
			try {
				latch.await(MAX_DELETION_PROCESS_DURATION_MS, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				logger.error("Could not wait for process to finish");
			}
		}

	}
}
