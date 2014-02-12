package org.hive2hive.core.processes.implementations.files.add;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.apache.log4j.Logger;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.api.configs.IFileConfiguration;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.Chunk;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.common.base.BasePutProcessStep;
import org.hive2hive.core.processes.implementations.context.AddFileProcessContext;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.HybridEncryptedContent;

/**
 * First validates the file size according to the limits set in the {@link IFileConfiguration} object.
 * Then, puts all chunks iteratively until all chunks are stored in the DHT.
 * This class is intended to be used for two scenarios:
 * <ul>
 * <li>A new file is uploaded</li>
 * <li>A new version of an existing is uploaded</li>
 * </ul>
 * The upload itself is not differing in these scenarios, but the subsequent steps might be.
 * 
 * @author Nico
 * 
 */
public class PutChunksStep extends BasePutProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(PutChunksStep.class);
	private final AddFileProcessContext context;
	private final IFileConfiguration config;

	public PutChunksStep(AddFileProcessContext context, IDataManager dataManager, IFileConfiguration config) {
		super(dataManager);
		this.context = context;
		this.config = config;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		List<KeyPair> chunkKeys = new ArrayList<KeyPair>();
		File file = context.getFile();

		// only put sth. if the file has content
		if (file.isDirectory()) {
			logger.debug("File " + file.getName() + ": No data to put because the file is a folder");
			return;
		} else if (context.consumeProtectionKeys() == null) {
			throw new ProcessExecutionException(
					"This directory is write protected (and we don't have the keys).");
		}

		// first, validate the file size
		long fileSize = FileUtil.getFileSize(file);
		if (fileSize > config.getMaxFileSize()) {
			throw new ProcessExecutionException("File is too large.");
		}

		long offset = 0;
		// TODO check if the cast is ok!
		byte[] data = new byte[(int) config.getChunkSize()];
		int read = 1;

		do {
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
				// create a chunk

				// the byte-Array may contain many empty slots if last chunk. Truncate it
				data = truncateData(data, read);

				KeyPair chunkKey = EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_CHUNK);
				Chunk chunk = new Chunk(chunkKey.getPublic(), data, chunkKeys.size(), read);
				chunkKeys.add(chunkKey);

				// more data to read (increase offset)
				offset += data.length;

				try {
					// encrypt the chunk prior to put such that nobody can read it
					HybridEncryptedContent encryptedContent = H2HEncryptionUtil.encryptHybrid(chunk,
							chunkKey.getPublic());

					// start the put and continue with next chunk
					logger.debug("Uploading chunk " + chunk.getOrder() + " of file " + file.getName());
					put(chunk.getId(), H2HConstants.FILE_CHUNK, encryptedContent,
							context.consumeProtectionKeys());
				} catch (IOException | DataLengthException | InvalidKeyException | IllegalStateException
						| InvalidCipherTextException | IllegalBlockSizeException | BadPaddingException
						| PutFailedException e) {
					logger.error("Could not encrypt and put the chunk", e);
					throw new ProcessExecutionException("Could not encrypt and put the chunk", e);
				}
			}
		} while (read > 0);

		logger.debug("File " + file.getName() + ": All chunks uploaded. Continue with meta data.");
		context.setChunkKeys(chunkKeys);
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
