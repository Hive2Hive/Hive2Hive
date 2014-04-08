package org.hive2hive.core.file.watcher;

import java.io.File;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.file.watcher.buffer.AddFileBuffer;
import org.hive2hive.core.file.watcher.buffer.DeleteFileBuffer;
import org.hive2hive.core.file.watcher.buffer.IFileBuffer;
import org.hive2hive.core.file.watcher.buffer.ModifyFileBuffer;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;

/**
 * Default implementation of a file listener. The file events are caught and the according process is
 * automatically started. Note that the processes are started even though the autostart may be turned off.
 * 
 * @author Christian
 * 
 */
public class H2HFileListener implements FileAlterationListener {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(H2HFileListener.class);

	private final IFileBuffer addFileBuffer;
	private final IFileBuffer deleteFileBuffer;
	private final ModifyFileBuffer modifyFileBuffer;

	public H2HFileListener(IFileManager fileManager) {
		addFileBuffer = new AddFileBuffer(fileManager);
		deleteFileBuffer = new DeleteFileBuffer(fileManager);
		modifyFileBuffer = new ModifyFileBuffer(fileManager);
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
		deleteFileBuffer.addFileToBuffer(directory);
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
			modifyFileBuffer.addFileToBuffer(file);
		}
	}

	@Override
	public void onFileDelete(File file) {
		printFileDetails("deleted", file);
		deleteFileBuffer.addFileToBuffer(file);
	}

	@Override
	public void onStop(FileAlterationObserver observer) {
		// nothing to do
	}

	private void printFileDetails(String reason, File file) {
		logger.debug(String.format("%s %s: %s\n", file.isDirectory() ? "Directory" : "File", reason,
				file.getAbsolutePath()));
	}

}
