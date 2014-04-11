package org.hive2hive.core.api;

import java.io.File;

import org.apache.commons.io.monitor.FileAlterationObserver;
import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.api.interfaces.IFileObserverListener;
import org.hive2hive.core.file.buffer.AddFileBuffer;
import org.hive2hive.core.file.buffer.DeleteFileBuffer;
import org.hive2hive.core.file.buffer.IFileBuffer;
import org.hive2hive.core.file.buffer.ModifyFileBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link IFileObserverListener}. The file events are caught and the according
 * process is automatically started.
 * 
 * @author Christian
 * 
 */
public class H2HFileObserverListener implements IFileObserverListener {

	private static final Logger logger = LoggerFactory.getLogger(H2HFileObserverListener.class);

	private final IFileBuffer addFileBuffer;
	private final IFileBuffer deleteFileBuffer;
	private final ModifyFileBuffer modifyFileBuffer;

	public H2HFileObserverListener(IFileManager fileManager) {
		this.addFileBuffer = new AddFileBuffer(fileManager);
		this.deleteFileBuffer = new DeleteFileBuffer(fileManager);
		this.modifyFileBuffer = new ModifyFileBuffer(fileManager);
	}

	@Override
	public void onStart(FileAlterationObserver observer) {
		// logger.debug("File observer for '{}' has been started.", observer.getDirectory().toPath()));
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
		// logger.debug("File observer for '{}' has been stopped.", observer.getDirectory().toPath()));
	}

	private void printFileDetails(String reason, File file) {
		logger.debug("{} {}: {}", file.isDirectory() ? "Directory" : "File", reason, file.getAbsolutePath());
	}
}
