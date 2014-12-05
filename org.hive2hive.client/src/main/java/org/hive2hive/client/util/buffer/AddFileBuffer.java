package org.hive2hive.client.util.buffer;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.processes.files.list.FileNode;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddFileBuffer extends BaseFileBuffer {

	private static final Logger logger = LoggerFactory.getLogger(AddFileBuffer.class);

	public AddFileBuffer(IFileManager fileManager) {
		super(fileManager);
	}

	@Override
	protected void processBuffer(IFileBufferHolder buffer) {
		Set<File> fileBuffer = filterBuffer(buffer.getFileBuffer(), buffer.getSyncFiles());

		// execute processes asynchronously
		IProcessComponent<?> addProcess;
		for (File toAdd : fileBuffer) {
			try {
				addProcess = fileManager.createAddProcess(toAdd);
			} catch (NoPeerConnectionException | NoSessionException | IllegalArgumentException ex) {
				logger.error("Cannot create a process to add '{}'.", toAdd.getName(), ex);
				continue;
			}
			try {
				addProcess.executeAsync(); // asynchronous
			} catch (InvalidProcessStateException ex) {
				logger.error("Cannot start the '{}' to add '{}'.", addProcess, toAdd.getName(), ex);
				continue;
			} catch (ProcessExecutionException ex) {
				logger.error("Process execution of '{}' to add '{}' failed.", addProcess, toAdd.getName(), ex);
				continue;
			}
		}
	}

	private Set<File> filterBuffer(List<File> fileBuffer, Set<FileNode> syncFiles) {
		// remove the files from the buffer which are already in the DHT
		// the event has been triggered by Hive2Hive when downloading it.
		for (FileNode syncFile : syncFiles) {
			fileBuffer.remove(syncFile.getFile());
		}

		Set<File> filtered = new HashSet<File>(fileBuffer);

		// only keep top-parent(s) to the buffer, filter out the rest
		for (File bufferedFile : fileBuffer) {
			// iterate through every file in buffer and check if there is a parent in the buffer
			for (File possibleParent : fileBuffer) {
				if (!bufferedFile.equals(possibleParent)
						&& bufferedFile.getAbsolutePath().startsWith(possibleParent.getAbsolutePath())) {
					logger.debug("Parent ({}) already in buffer, no need to add child ({}), too.",
							possibleParent.getAbsolutePath(), bufferedFile.getAbsolutePath());
					filtered.remove(bufferedFile);
				}
			}
		}

		return filtered;
	}

}
