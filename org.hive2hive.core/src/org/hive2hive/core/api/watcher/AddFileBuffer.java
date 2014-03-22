package org.hive2hive.core.api.watcher;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArraySet;

import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponent;

public class AddFileBuffer implements IFileBuffer {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(AddFileBuffer.class);

	private final IFileManager fileManager;
	private final Set<File> fileBuffer;

	public AddFileBuffer(IFileManager fileManager) {
		this.fileManager = fileManager;
		this.fileBuffer = new CopyOnWriteArraySet<File>();
	}

	@Override
	public void addFileToBuffer(File file) {
		if (fileBuffer.isEmpty()) {
			// was the first event --> trigger the upload
			fileBuffer.add(file);

			// wait until possibly other events came in
			new Timer().schedule(new TimerTask() {
				@Override
				public void run() {
					logger.debug("Finished buffering. " + fileBuffer.size() + " file(s) in buffer");

					// clear the buffer for next trigger before processing because the method's implementation
					// could be slow and / or blocking.
					ArrayList<File> antiRaceCopy = new ArrayList<File>(fileBuffer);
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
			if (!parentInBuffer) {
				// remove children from buffer
				for (File bufferedFile : fileBuffer) {
					if (bufferedFile.getAbsolutePath().startsWith(file.getAbsolutePath())) {
						logger.debug("Remove child " + bufferedFile.getAbsolutePath()
								+ " from buffer because parent is now added.");
						fileBuffer.remove(bufferedFile);
					}
				}

				// add the new file to the buffer
				fileBuffer.add(file);
			}
		}
	}

	/**
	 * Process the files in the buffer after the buffering time exceeded.
	 * 
	 * @param bufferedFiles
	 */
	private void processBufferedFiles(List<File> bufferedFiles) {
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
