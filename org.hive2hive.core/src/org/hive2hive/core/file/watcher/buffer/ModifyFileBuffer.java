package org.hive2hive.core.file.watcher.buffer;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponent;

public class ModifyFileBuffer extends BaseFileBuffer {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(ModifyFileBuffer.class);

	public ModifyFileBuffer(IFileManager fileManager, File root) {
		super(fileManager, root);
	}

	@Override
	protected void processBuffer(IFileBufferHolder buffer) {
		List<File> fileBuffer = buffer.getFileBuffer();
		Set<File> toDelete = new HashSet<File>();
		for (File file : fileBuffer) {
			if (!buffer.getSyncFiles().contains(file)) {
				// don't modify a file that is not in the DHT
				toDelete.add(file);
			}

			// TODO: check for MD5 hashes, if equal, skip the file
		}

		for (File file : fileBuffer) {
			try {
				IProcessComponent process = fileManager.update(file);
				if (!fileManager.isAutostart()) {
					process.start();
				}
			} catch (IllegalArgumentException | NoSessionException | NoPeerConnectionException
					| InvalidProcessStateException e) {
				logger.error(e.getMessage());
			}
		}
	}
}
