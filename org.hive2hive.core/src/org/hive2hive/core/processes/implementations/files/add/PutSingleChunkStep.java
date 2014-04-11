package org.hive2hive.core.processes.implementations.files.add;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.model.Chunk;
import org.hive2hive.core.model.MetaChunk;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.common.base.BasePutProcessStep;
import org.hive2hive.core.processes.implementations.context.AddFileProcessContext;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.HybridEncryptedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Puts a single chunk without storing it anywhere (thus large files should be no problem).
 * 
 * @author Nico, Seppi
 */
public class PutSingleChunkStep extends BasePutProcessStep {

	private final static Logger logger = LoggerFactory.getLogger(PutSingleChunkStep.class);

	private final int index;
	private final AddFileProcessContext context;
	private final IFileConfiguration config;
	private final String chunkId;

	public PutSingleChunkStep(AddFileProcessContext context, int index, String chunkId,
			IDataManager dataManager, IFileConfiguration config) {
		super(dataManager);
		this.index = index;
		this.context = context;
		this.chunkId = chunkId;
		this.config = config;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		File file = context.getFile();
		int read = 0;
		long offset = config.getChunkSize() * index;
		byte[] data = new byte[config.getChunkSize()];

		try {
			// read the next chunk of the file considering the offset
			RandomAccessFile rndAccessFile = new RandomAccessFile(file, "r");
			rndAccessFile.seek(offset);
			read = rndAccessFile.read(data);
			rndAccessFile.close();
		} catch (IOException e) {
			logger.error("File {}: Could not read the file.", file.getAbsolutePath());
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
						.consumeChunkKeys().getPublic());

				logger.debug("Uploading chunk {} of file {}.", chunk.getOrder(), file.getName());
				Parameters parameters = new Parameters().setLocationKey(chunk.getId())
						.setContentKey(H2HConstants.FILE_CHUNK).setData(encryptedContent)
						.setProtectionKeys(context.consumeProtectionKeys()).setTTL(chunk.getTimeToLive());
				// data manager has to produce the hash, which gets used for signing
				parameters.setHashFlag(true);
				// put the encrypted chunk into the network
				put(parameters);
				// store the hash in the index of the meta file
				context.getMetaChunks().add(new MetaChunk(chunkId, parameters.getHash()));
			} catch (IOException | DataLengthException | InvalidKeyException | IllegalStateException
					| InvalidCipherTextException | IllegalBlockSizeException | BadPaddingException
					| PutFailedException e) {
				logger.error("Could not encrypt and put the chunk.", e);
				throw new ProcessExecutionException("Could not encrypt and put the chunk.", e);
			}
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
