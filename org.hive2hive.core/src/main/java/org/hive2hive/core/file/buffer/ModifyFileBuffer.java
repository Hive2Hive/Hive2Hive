package org.hive2hive.core.file.buffer;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponent;
import org.hive2hive.core.processes.implementations.files.list.FileTaste;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.H2HDefaultEncryption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
					byte[] fileHash = EncryptionUtil.generateMD5Hash(file);
					if (H2HDefaultEncryption.compareMD5(fileHash, fileTaste.getMd5())) {
						// hashes are equal, no need to upload it to the DHT
						toDelete.add(file);
					}
				} catch (IOException e) {
					logger.warn("Could not generate the MD5 hash of the file to be able to compare against the file taste.");
				}
			}
		}

		fileBuffer.removeAll(toDelete);

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
