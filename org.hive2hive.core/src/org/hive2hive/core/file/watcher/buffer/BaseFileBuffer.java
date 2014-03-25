package org.hive2hive.core.file.watcher.buffer;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.interfaces.IProcessResultListener;
import org.hive2hive.core.processes.framework.interfaces.IResultProcessComponent;

public abstract class BaseFileBuffer implements IFileBuffer {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(BaseFileBuffer.class);

	protected final IFileManager fileManager;
	protected FileBufferHolder currentBuffer;
	private final File root;

	protected BaseFileBuffer(IFileManager fileManager, File root) {
		this.fileManager = fileManager;
		this.root = root;
	}

	@Override
	public final void addFileToBuffer(File file) {
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
		new Thread(new Runnable() {
			@Override
			public void run() {
				IResultProcessComponent<List<Path>> fileList = fileManager.getFileList();
				fileList.attachListener(new IProcessResultListener<List<Path>>() {
					@Override
					public void onResultReady(List<Path> result) {
						Set<File> syncFiles = new HashSet<File>(result.size());
						for (Path path : result) {
							syncFiles.add(new File(root, path.toString()));
						}

						// the result is ready, add it to the buffer
						fileBuffer.setSyncFiles(syncFiles);
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
		}).start();
	}

	/**
	 * Process the files in the buffer after the buffering time exceeded.
	 * 
	 * @param bufferedFiles
	 */
	protected abstract void processBuffer(IFileBufferHolder buffer);

}
