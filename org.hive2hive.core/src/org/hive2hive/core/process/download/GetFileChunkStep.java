package org.hive2hive.core.process.download;

import java.io.IOException;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.Chunk;
import org.hive2hive.core.model.FileVersion;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.process.common.get.BaseGetProcessStep;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.HybridEncryptedContent;

public class GetFileChunkStep extends BaseGetProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(GetFileChunkStep.class);

	private final Path path;
	private final List<KeyPair> chunkKeys;
	private final List<Chunk> chunkBuffer;
	private int currentChunkOrder;

	public GetFileChunkStep(Path destination, FileVersion version) {
		this.path = destination;
		this.chunkKeys = version.getChunkIds();
		this.currentChunkOrder = 0;
		this.chunkBuffer = new ArrayList<Chunk>();
	}

	@Override
	public void start() {
		int counter = 0;
		for (KeyPair chunkKey : chunkKeys) {
			logger.info("File " + path + ": Downloading chunk " + counter++ + "/" + chunkKeys.size());
			NetworkContent content = get(key2String(chunkKey.getPublic()), H2HConstants.FILE_CHUNK);
			HybridEncryptedContent encrypted = (HybridEncryptedContent) content;
			try {
				NetworkContent decrypted = H2HEncryptionUtil.decryptHybrid(encrypted, chunkKey.getPrivate());
				chunkBuffer.add((Chunk) decrypted);
				writeBufferToDisk();
			} catch (InvalidKeyException | DataLengthException | IllegalBlockSizeException
					| BadPaddingException | IllegalStateException | InvalidCipherTextException
					| IllegalArgumentException e) {
				getProcess().stop("Could not decrypt file chunk. Reason: " + e.getMessage());
				return;
			} catch (IOException e) {
				getProcess().stop("Could not write file chunk. Reason: " + e.getMessage());
				return;
			}
		}

		// all chunks downloaded
		if (chunkBuffer.isEmpty()) {
			// normal case: done with the process.
			logger.debug("Finished downloading file '" + path + "'.");
			getProcess().setNextStep(null);
		} else {
			// should be empty
			logger.error("All chunks downloaded but still some in buffer.");
			getProcess()
					.stop("Could not write all chunks to disk. We're stuck at chunk " + currentChunkOrder);
		}
	}

	/**
	 * Writes the buffered chunks to the disk (in the correct order)
	 * 
	 * @throws IOException
	 */
	private void writeBufferToDisk() throws IOException {
		List<Chunk> wroteToDisk = new ArrayList<Chunk>();
		do {
			wroteToDisk.clear();
			for (Chunk chunk : chunkBuffer) {
				if (chunk.getOrder() == currentChunkOrder) {
					// append only if already written a chunk, else overwrite the possibly existent file
					boolean append = currentChunkOrder != 0;

					FileUtils.writeByteArrayToFile(path.toFile(), chunk.getData(), append);
					wroteToDisk.add(chunk);
					currentChunkOrder++;
				}
			}

			chunkBuffer.removeAll(wroteToDisk);
		} while (!wroteToDisk.isEmpty());
	}
}
