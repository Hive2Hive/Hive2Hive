package org.hive2hive.core.process.download;

import java.io.IOException;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.PrivateKey;
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
	private final List<KeyPair> chunksToGet;
	private final List<Chunk> chunkBuffer;
	private int currentChunkOrder;
	private PrivateKey decryptionKey;

	/**
	 * Constructor for first step
	 * 
	 * @param destination
	 * @param version
	 */
	public GetFileChunkStep(Path destination, FileVersion version) {
		this(destination, 0, version.getChunkIds(), new ArrayList<Chunk>());
		logger.debug("Start downloading '" + destination.toString() + "'");
	}

	/**
	 * Constructor when multiple chunks are in the DHT that need to be download
	 * 
	 * @param file
	 * @param currentChunkOrder
	 * @param chunksToGet
	 * @param chunkBuffer
	 */
	private GetFileChunkStep(Path path, int currentChunkOrder, List<KeyPair> chunksToGet,
			List<Chunk> chunkBuffer) {
		this.path = path;
		this.currentChunkOrder = currentChunkOrder;
		this.chunksToGet = chunksToGet;
		this.chunkBuffer = chunkBuffer;
	}

	@Override
	public void start() {
		// download next chunk
		KeyPair firstInList = chunksToGet.remove(0);
		decryptionKey = firstInList.getPrivate(); // store current private key
		logger.info("File " + path + ": " + chunksToGet.size() + " chunk(s) more to download.");
		NetworkContent content = get(key2String(firstInList.getPublic()), H2HConstants.FILE_CHUNK);
		evaluateResult(content);
	}

	public void evaluateResult(NetworkContent content) {
		HybridEncryptedContent encrypted = (HybridEncryptedContent) content;
		try {
			NetworkContent decrypted = H2HEncryptionUtil.decryptHybrid(encrypted, decryptionKey);
			chunkBuffer.add((Chunk) decrypted);
			writeBufferToDisk();

			if (chunksToGet.isEmpty()) {
				// all chunks downloaded
				if (chunkBuffer.isEmpty()) {
					// normal case: done with the process.
					logger.debug("Finished downloading file '" + path + "'.");
					getProcess().setNextStep(null);
				} else {
					// should be empty
					logger.error("All chunks downloaded but still some in buffer.");
					getProcess().stop(
							"Could not write all chunks to disk. We're stuck at chunk " + currentChunkOrder);
				}
			} else {
				// more chunks to get. Continue with downloadint the next chunk
				GetFileChunkStep nextStep = new GetFileChunkStep(path, currentChunkOrder, chunksToGet,
						chunkBuffer);
				getProcess().setNextStep(nextStep);
			}
		} catch (InvalidKeyException | DataLengthException | IllegalBlockSizeException | BadPaddingException
				| IllegalStateException | InvalidCipherTextException | IllegalArgumentException e) {
			getProcess().stop("Could not decrypt file chunk. Reason: " + e.getMessage());
		} catch (IOException e) {
			getProcess().stop("Could not write file chunk. Reason: " + e.getMessage());
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
