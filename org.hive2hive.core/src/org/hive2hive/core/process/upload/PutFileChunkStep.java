package org.hive2hive.core.process.upload;

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
import org.hive2hive.core.IH2HFileConfiguration;
import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.Chunk;
import org.hive2hive.core.process.common.put.PutProcessStep;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.EncryptionUtil.AES_KEYLENGTH;
import org.hive2hive.core.security.EncryptionUtil.RSA_KEYLENGTH;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.HybridEncryptedContent;

/**
 * First validates the file size according to the limits set in the {@link IH2HFileConfiguration} object.
 * Then, puts a chunk and recursively calls itself until all chunks are stored in the DHT.
 * 
 * @author Nico
 * 
 */
public class PutFileChunkStep extends PutProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(PutFileChunkStep.class);

	protected final File file;
	private final long offset;
	protected final List<KeyPair> chunkKeys;

	/**
	 * Constructor for first call
	 * 
	 * @param file
	 */
	public PutFileChunkStep(File file) {
		this(file, 0, new ArrayList<KeyPair>());
	}

	/**
	 * Constructor needed when file has multiple chunks
	 * 
	 * @param file
	 * @param offset
	 * @param chunkKeys
	 */
	protected PutFileChunkStep(File file, long offset, List<KeyPair> chunkKeys) {
		// the details are set later
		super(null, H2HConstants.FILE_CHUNK, null, null);
		this.file = file;
		this.offset = offset;
		this.chunkKeys = chunkKeys;
	}

	@Override
	public void start() {
		// only put sth. if the file has content
		if (file.isDirectory()) {
			logger.debug("Nothing to put since the file is a folder");
			return;
		}

		BaseUploadFileProcessContext context = (BaseUploadFileProcessContext) getProcess().getContext();

		// first, validate the file size (only first time)
		if (chunkKeys.isEmpty()) {
			IH2HFileConfiguration config = context.getConfig();
			long fileSize = FileUtil.getFileSize(file);

			if (fileSize > config.getMaxFileSize()) {
				getProcess().stop("File is too large");
				return;
			}
		}

		byte[] data = new byte[context.getConfig().getChunkSize()];
		int read = -1;
		try {
			// read the next chunk of the file considering the offset
			RandomAccessFile rndAccessFile = new RandomAccessFile(file, "r");
			rndAccessFile.seek(offset);
			read = rndAccessFile.read(data);
			rndAccessFile.close();
		} catch (IOException e) {
			logger.error("Could not read the file", e);
			getProcess().stop(e.getMessage());
			return;
		}

		if (read > 0) {
			// create a chunk

			// the byte-Array may contain many empty slots if last chunk. Truncate it
			data = truncateData(data, read);

			KeyPair chunkKey = EncryptionUtil.generateRSAKeyPair(RSA_KEYLENGTH.BIT_2048);
			Chunk chunk = new Chunk(chunkKey.getPublic(), data, chunkKeys.size(), read);
			chunkKeys.add(chunkKey);

			// more data to read (increase offset)
			nextStep = new PutFileChunkStep(file, offset + data.length, chunkKeys);

			try {
				// encrypt the chunk prior to put such that nobody can read it
				HybridEncryptedContent encryptedContent = H2HEncryptionUtil.encryptHybrid(chunk,
						chunkKey.getPublic(), AES_KEYLENGTH.BIT_256);

				// start the put and continue with next chunk
				logger.debug("Uploading chunk " + chunk.getOrder() + " of file " + file.getAbsolutePath());
				put(key2String(chunk.getId()), H2HConstants.FILE_CHUNK, encryptedContent);
			} catch (DataLengthException | InvalidKeyException | IllegalStateException
					| InvalidCipherTextException | IllegalBlockSizeException | BadPaddingException e) {
				logger.error("Could not encrypt the chunk", e);
				getProcess().stop(e.getMessage());
			}
		} else {
			logger.debug("All chunks uploaded. Continue with meta data.");
			// nothing read, stop putting chunks and start next step
			context.setChunkKeys(chunkKeys);
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
