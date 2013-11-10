package org.hive2hive.core.process.download;

import java.io.File;
import java.io.IOException;
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
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.Chunk;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.process.common.get.BaseGetProcessStep;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.HybridEncryptedContent;

public class GetFileChunkStep extends BaseGetProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(GetFileChunkStep.class);

	private final File file;
	private final List<KeyPair> chunksToGet;
	private final List<Chunk> chunkBuffer;
	private int currentChunkOrder;
	private PrivateKey decryptionKey;

	/**
	 * Constructor for first step
	 * 
	 * @param file
	 * @param metaFile
	 * @param fileManager
	 */
	public GetFileChunkStep(FileTreeNode file, MetaFile metaFile, FileManager fileManager) {
		this(fileManager.getFile(file), 0, metaFile.getNewestVersion().getChunkIds(), new ArrayList<Chunk>());
	}

	/**
	 * Constructor when multiple chunks are in the DHT that need to be download
	 * 
	 * @param file
	 * @param currentChunkOrder
	 * @param chunksToGet
	 * @param chunkBuffer
	 */
	private GetFileChunkStep(File file, int currentChunkOrder, List<KeyPair> chunksToGet,
			List<Chunk> chunkBuffer) {
		super(null, H2HConstants.FILE_CHUNK);
		this.file = file;
		this.currentChunkOrder = currentChunkOrder;
		this.chunksToGet = chunksToGet;
		this.chunkBuffer = chunkBuffer;
	}

	@Override
	public void start() {
		// download next chunk
		KeyPair firstInList = chunksToGet.remove(0);
		decryptionKey = firstInList.getPrivate(); // store current private key
		logger.info("Downloading next chunk... " + chunksToGet.size() + " chunk(s) more to go.");
		get(firstInList.getPublic().toString(), H2HConstants.FILE_CHUNK);
	}

	@Override
	protected void handleGetResult(NetworkContent content) {
		HybridEncryptedContent encrypted = (HybridEncryptedContent) content;
		try {
			NetworkContent decrypted = H2HEncryptionUtil.decryptHybrid(encrypted, decryptionKey);
			chunkBuffer.add((Chunk) decrypted);
			writeBufferToDisk();

			if (chunksToGet.isEmpty()) {
				// all chunks downloaded
				if (chunkBuffer.isEmpty()) {
					// should be empty
					logger.error("All chunks downloaded but still some in buffer.");
					getProcess().stop(
							"Could not write all chunks to disk. We're stuck at chunk " + currentChunkOrder);
				} else {
					// normal case: done with the process.
					getProcess().setNextStep(null);
				}
			} else {
				// more chunks to get. Continue with downloadint the next chunk
				GetFileChunkStep nextStep = new GetFileChunkStep(file, currentChunkOrder, chunksToGet,
						chunkBuffer);
				getProcess().setNextStep(nextStep);
			}
		} catch (InvalidKeyException | DataLengthException | IllegalBlockSizeException | BadPaddingException
				| IllegalStateException | InvalidCipherTextException e) {
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
		boolean wroteSth = false;
		do {
			for (Chunk chunk : chunkBuffer) {
				if (chunk.getOrder() == currentChunkOrder) {
					FileUtils.writeByteArrayToFile(file, chunk.getData(), true);
					currentChunkOrder++;
				}
			}
		} while (wroteSth);
	}
}
