package org.hive2hive.core.api.watcher;

import java.io.File;
import java.util.List;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponent;

/**
 * Default implementation of a file listener. The file events are caught and the according process is
 * automatically started. Note that the processes are started even though the autostart may be turned off.
 * 
 * @author Nico
 * 
 */
public class H2HFileListener implements FileAlterationListener {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(H2HFileListener.class);

	private final IFileManager fileManager;
	private FileEventBuffer addFileBuffer;

	public H2HFileListener(IFileManager fileManager) {
		this.fileManager = fileManager;
		addFileBuffer = new AddFileBuffer();
	}

	@Override
	public void onStart(FileAlterationObserver observer) {
	}

	@Override
	public void onDirectoryCreate(File directory) {
		printFileDetails("created", directory);
		addFileBuffer.addFileToBuffer(directory);
	}

	@Override
	public void onDirectoryChange(File directory) {
		// ignore
	}

	@Override
	public void onDirectoryDelete(File directory) {
		printFileDetails("deleted", directory);
		removeFile(directory);
	}

	@Override
	public void onFileCreate(File file) {
		printFileDetails("created", file);
		addFileBuffer.addFileToBuffer(file);
	}

	@Override
	public void onFileChange(File file) {
		if (file.isFile()) {
			printFileDetails("changed", file);
			modifyFile(file);
		}
	}

	@Override
	public void onFileDelete(File file) {
		printFileDetails("deleted", file);
		removeFile(file);
	}

	@Override
	public void onStop(FileAlterationObserver observer) {
		// nothing to do
	}

	private void removeFile(File file) {
		try {
			fileManager.delete(file);
		} catch (IllegalArgumentException | NoSessionException | NoPeerConnectionException e) {
			logger.error(e.getMessage());
		}
	}

	private void modifyFile(File file) {
		try {
			fileManager.update(file);
		} catch (IllegalArgumentException | NoSessionException | NoPeerConnectionException e) {
			logger.error(e.getMessage());
		}
	}

	private void printFileDetails(String reason, File file) {
		logger.debug(String.format("%s %s: %s\n", file.isDirectory() ? "Directory" : "File", reason,
				file.getAbsolutePath()));
	}

	private class AddFileBuffer extends FileEventBuffer {

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
}
