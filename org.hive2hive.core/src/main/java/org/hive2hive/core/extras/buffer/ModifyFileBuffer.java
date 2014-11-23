package org.hive2hive.core.extras.buffer;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.extras.Extra;
import org.hive2hive.core.processes.files.list.FileTaste;
import org.hive2hive.core.security.HashUtil;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Extra
public class ModifyFileBuffer extends BaseFileBuffer {

	private static final Logger logger = LoggerFactory.getLogger(ModifyFileBuffer.class);

	public ModifyFileBuffer(IFileManager fileManager) {
		super(fileManager);
	}

	@Override
	protected void processBuffer(IFileBufferHolder buffer) {
		List<File> fileBuffer = buffer.getFileBuffer();
		Set<FileTaste> syncFiles = buffer.getSyncFiles();

		/**
		 * Start the verification: remove files that are not in the DHT yet and remove files that equal to the
		 * ones in the DHT.
		 */
		Set<File> toDelete = new HashSet<File>();
		for (File file : fileBuffer) {
			FileTaste fileTaste = null;
			for (FileTaste syncFile : syncFiles) {
				if (syncFile.getFile().equals(file)) {
					fileTaste = syncFile;
					break;
				}
			}

			if (fileTaste == null) {
				// don't modify a file that is not in the DHT
				toDelete.add(file);
			} else {
				try {
					// check for MD5 hashes, if equal, skip the file
					byte[] fileHash = HashUtil.hash(file);
					if (HashUtil.compare(fileHash, fileTaste.getMd5())) {
						// hashes are equal, no need to upload it to the DHT
						toDelete.add(file);
					}
				} catch (IOException e) {
					logger.warn("Could not generate the hash of the file to be able to compare against the file taste.", e);
				}
			}
		}

		fileBuffer.removeAll(toDelete);

		// execute processes asynchronously
		IProcessComponent<?> updateProcess;
		for (File toUpdate : fileBuffer) {
			try {
				updateProcess = fileManager.createUpdateProcess(toUpdate);
			} catch (NoSessionException | NoPeerConnectionException | IllegalArgumentException ex) {
				logger.error("Cannot create a process to add '{}'.", toUpdate.getName(), ex);
				continue;
			}
			try {
				updateProcess.executeAsync(); // asynchronous
			} catch (InvalidProcessStateException ex) {
				logger.error("Cannot start the '{}' to update '{}'.", updateProcess, toUpdate.getName(), ex);
				continue;
			} catch (ProcessExecutionException ex) {
				logger.error("Process execution of '{}' to update '{}' failed.", updateProcess, toUpdate.getName(), ex);
				continue;
			}
		}
	}
}
