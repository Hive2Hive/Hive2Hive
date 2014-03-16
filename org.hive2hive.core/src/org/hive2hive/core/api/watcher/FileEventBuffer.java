package org.hive2hive.core.api.watcher;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;

public abstract class FileEventBuffer {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(FileEventBuffer.class);
	private static final long BUFFER_WAIT_TIME_MS = 3000;

	private final List<File> fileBuffer;

	public FileEventBuffer() {
		this.fileBuffer = new CopyOnWriteArrayList<File>();
	}

	/**
	 * The file is added to a buffer which waits a certain time. This ensures that the listener is not called
	 * for both parent folder and containing files, causing race-conditions at Hive2Hive.
	 * 
	 * @param file
	 */
	public void addFileToBuffer(File file) {
		if (fileBuffer.isEmpty()) {
			// was the first event --> trigger the upload
			fileBuffer.add(file);

			// wait until possibly other events came in
			new Timer().schedule(new TimerTask() {
				@Override
				public void run() {
					logger.debug("Finished buffering. " + fileBuffer.size() + " file(s) in biffer");

					// clear the buffer for next trigger before processing because the method's implementation
					// could be slow and / or blocking.
					ArrayList<File> antiRaceCopy = new ArrayList<>(fileBuffer);
					fileBuffer.clear();

					processBufferedFiles(antiRaceCopy);

				}
			}, BUFFER_WAIT_TIME_MS);

			logger.debug("Start buffering succeeding add file events (" + BUFFER_WAIT_TIME_MS + "ms)");
		} else {
			// add it to the file buffer if there is not already a parent there; the parent event will trigger
			// the upload of the file
			boolean parentInBuffer = false;
			for (File bufferedFile : fileBuffer) {
				if (file.getAbsolutePath().startsWith(bufferedFile.getAbsolutePath())) {
					logger.debug("Parent (" + bufferedFile.getAbsolutePath()
							+ ") already in buffer, no need to add " + file.getAbsolutePath() + " too.");
					parentInBuffer = true;
					break;
				}
			}

			// if parent in buffer --> skip adding itself to the buffer
			// else --> add to buffer, first in buffer triggers the add file soon
			if (!parentInBuffer)
				fileBuffer.add(file);
		}
	}

	/**
	 * Process the files in the buffer after the buffering time exceeded.
	 * 
	 * @param bufferedFiles
	 */
	protected abstract void processBufferedFiles(List<File> bufferedFiles);
}
