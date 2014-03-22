package org.hive2hive.core.api.watcher;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArraySet;

import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;

public abstract class BaseFileBuffer implements IFileBuffer {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(BaseFileBuffer.class);
	protected final Set<File> fileBuffer;

	protected BaseFileBuffer() {
		this.fileBuffer = new CopyOnWriteArraySet<File>();
	}

	@Override
	public final void addFileToBuffer(File file) {
		if (needsBufferInsertion(file)) {
			if (fileBuffer.isEmpty())
				startBuffering();

			fileBuffer.add(file);
		}
	}

	private void startBuffering() {
		logger.debug("Start buffering for " + IFileBuffer.BUFFER_WAIT_TIME_MS + " ms");
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				logger.debug("Finished buffering. " + fileBuffer.size() + " file(s) in buffer");

				// clear the buffer for next trigger before processing because the method's implementation
				// could be slow and / or blocking.
				ArrayList<File> antiRaceCopy = new ArrayList<File>(fileBuffer);
				fileBuffer.clear();
				logger.debug("Ready for next buffered operation");

				if (!antiRaceCopy.isEmpty())
					processBufferedFiles(antiRaceCopy);
			}
		}, BUFFER_WAIT_TIME_MS);
	}

	/**
	 * Asks the implementation whether the file needs to be inserted to the buffer
	 * 
	 * @param file
	 * @return true when the file should be inserted to buffer
	 */
	protected abstract boolean needsBufferInsertion(File file);

	/**
	 * Process the files in the buffer after the buffering time exceeded.
	 * 
	 * @param bufferedFiles
	 */
	protected abstract void processBufferedFiles(List<File> bufferedFiles);
}
