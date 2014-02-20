package org.hive2hive.core.processes.implementations.files.add;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.apache.log4j.Logger;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.Chunk;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.context.AddFileProcessContext;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.HybridEncryptedContent;

/**
 * Puts a single chunk without storing it anywhere (thus large files should be no problem).
 * 
 * @author Nico
 * 
 */
public class PutSingleChunkStep extends ProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(PutSingleChunkStep.class);

	private final int index;
	private final AddFileProcessContext context;
	private final IFileConfiguration config;
	private IDataManager dataManager;
	private final String chunkId;

	// for rollback
	private boolean putPerformed;

	public PutSingleChunkStep(AddFileProcessContext context, int index, String chunkId,
			IDataManager dataManager, IFileConfiguration config) {
		this.index = index;
		this.context = context;
		this.chunkId = chunkId;
		this.dataManager = dataManager;
		this.config = config;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		File file = context.getFile();
		int read = 0;
		long offset = config.getChunkSize() * index;
		// TODO check if the cast is ok!
		byte[] data = new byte[(int) config.getChunkSize()];

		try {
			// read the next chunk of the file considering the offset
			RandomAccessFile rndAccessFile = new RandomAccessFile(file, "r");
			rndAccessFile.seek(offset);
			read = rndAccessFile.read(data);
			rndAccessFile.close();
		} catch (IOException e) {
			logger.error("File " + file.getAbsolutePath() + ": Could not read the file", e);
			throw new ProcessExecutionException("File " + file.getAbsolutePath()
					+ ": Could not read the file", e);
		}

		if (read > 0) {
			// the byte-Array may contain many empty slots if last chunk. Truncate it
			data = truncateData(data, read);
			// create a chunk
			Chunk chunk = new Chunk(chunkId, data, index, read);

			try {
				// encrypt the chunk prior to put such that nobody can read it
				HybridEncryptedContent encryptedContent = H2HEncryptionUtil.encryptHybrid(chunk, context
						.getChunkEncryptionKeys().getPublic());

				logger.debug("Uploading chunk " + chunk.getOrder() + " of file " + file.getName());
				boolean success = dataManager.put(chunk.getId(), H2HConstants.FILE_CHUNK, encryptedContent,
						context.consumeProtectionKeys());
				putPerformed = true;

				if (!success) {
					throw new PutFailedException();
				}
			} catch (IOException | DataLengthException | InvalidKeyException | IllegalStateException
					| InvalidCipherTextException | IllegalBlockSizeException | BadPaddingException
					| PutFailedException e) {
				logger.error("Could not encrypt and put the chunk", e);
				throw new ProcessExecutionException("Could not encrypt and put the chunk", e);
			}
		}
	}

	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {
		if (!putPerformed) {
			// nothing to rollback
			return;
		}

		boolean success = dataManager.remove(chunkId, H2HConstants.FILE_CHUNK,
				context.consumeProtectionKeys());
		if (success) {
			logger.debug("Rollback of putting the chunk succeeded.");
		} else {
			logger.warn("Rollback of putting the chunk failed.");
		}
	}

	/**
	 * Truncates a byte array
	 * 
	 * @param data
	 * @param read
	 * @return a shorter byte array
	 */
	private byte[] truncateData(byte[] data, int numOfBytes) {
		// shortcut
		if (data.length == numOfBytes) {
			return data;
		} else {
			byte[] truncated = new byte[numOfBytes];
			for (int i = 0; i < truncated.length; i++) {
				truncated[i] = data[i];
			}
			return truncated;
		}
	}

}
