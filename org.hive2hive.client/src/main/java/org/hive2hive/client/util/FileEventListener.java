package org.hive2hive.client.util;

import java.io.IOException;

import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.events.framework.interfaces.IFileEventListener;
import org.hive2hive.core.events.framework.interfaces.file.IFileAddEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileDeleteEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileMoveEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileShareEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileUpdateEvent;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.interfaces.IProcessComponent;

@Listener(references = References.Strong)
public class FileEventListener implements IFileEventListener {

	private final IFileManager fileManager;

	public FileEventListener(IFileManager fileManager) {
		this.fileManager = fileManager;
	}

	@Override
	@Handler
	public void onFileAdd(IFileAddEvent fileEvent) {
		try {
			IProcessComponent<Void> downloadProcess = fileManager.createDownloadProcess(fileEvent.getFile());
			downloadProcess.execute();

		} catch (InvalidProcessStateException | ProcessExecutionException | NoSessionException | NoPeerConnectionException e) {
			System.err.println("Cannot download the new file " + fileEvent.getFile());
		}
	}

	@Override
	@Handler
	public void onFileUpdate(IFileUpdateEvent fileEvent) {
		try {
			IProcessComponent<Void> downloadProcess = fileManager.createDownloadProcess(fileEvent.getFile());
			downloadProcess.execute();

		} catch (InvalidProcessStateException | ProcessExecutionException | NoSessionException | NoPeerConnectionException e) {
			System.err.println("Cannot download the updated file " + fileEvent.getFile());
		}
	}

	@Override
	@Handler
	public void onFileDelete(IFileDeleteEvent fileEvent) {
		if (fileEvent.getFile().delete()) {
			System.out.println("Deleted file " + fileEvent.getFile());
		} else {
			System.err.println("Could not delete file " + fileEvent.getFile());
		}
	}

	@Override
	@Handler
	public void onFileMove(IFileMoveEvent fileEvent) {
		try {
			if (fileEvent.isFile()) {
				FileUtils.moveFile(fileEvent.getSrcFile(), fileEvent.getDstFile());
			} else {
				FileUtils.moveDirectory(fileEvent.getSrcFile(), fileEvent.getDstFile());
			}
		} catch (IOException e) {
			System.err.println("Cannot move the file / folder " + fileEvent.getFile());
		}
	}

	@Override
	@Handler
	public void onFileShare(IFileShareEvent fileEvent) {
		// ignore because it will trigger onFileAdd for every file anyhow
	}

}
