package org.hive2hive.core.processes.implementations.files.download;

import java.io.File;
import java.io.IOException;
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
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.Chunk;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.implementations.common.base.BaseGetProcessStep;
import org.hive2hive.core.processes.implementations.context.DownloadFileContext;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.HybridEncryptedContent;

public class DownloadChunksStep extends BaseGetProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(DownloadChunksStep.class);

	private final DownloadFileContext context;
	private final List<Chunk> chunkBuffer;
	private final FileManager fileManager;
	private int currentChunkOrder;
	private File destination;

	public DownloadChunksStep(DownloadFileContext context, IDataManager dataManager, FileManager fileManager) {
		super(dataManager);
		this.context = context;
		this.fileManager = fileManager;
		this.currentChunkOrder = 0;
		this.chunkBuffer = new ArrayList<Chunk>();
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		MetaFile metaFile = (MetaFile) context.consumeMetaDocument();

		// support to download a specific version
		int versionToDownload = context.getVersionToDownload();
		List<KeyPair> chunkKeys = metaFile.getNewestVersion().getChunkKeys();
		if (versionToDownload != DownloadFileContext.NEWEST_VERSION_INDEX) {
			chunkKeys = metaFile.getVersionByIndex(versionToDownload).getChunkKeys();
		}

		// support to store the file on another location than default (used for recover)
		destination = fileManager.getPath(context.getFileNode()).toFile();
		if (context.getDestination() != null) {
			destination = context.getDestination();
		}

		if (!verifyFile(destination)) {
			cancel(new RollbackReason(this,
					"File already exists on disk. Content does match; no download needed"));
			return;
		}

		// start the download
		int counter = 0;
		for (KeyPair chunkKey : chunkKeys) {
			logger.info("File " + destination + ": Downloading chunk " + counter++ + "/" + chunkKeys.size());
			NetworkContent content = get(H2HEncryptionUtil.key2String(chunkKey.getPublic()),
					H2HConstants.FILE_CHUNK);
			HybridEncryptedContent encrypted = (HybridEncryptedContent) content;
			try {
				NetworkContent decrypted = H2HEncryptionUtil.decryptHybrid(encrypted, chunkKey.getPrivate());
				chunkBuffer.add((Chunk) decrypted);
				writeBufferToDisk();
			} catch (ClassNotFoundException | InvalidKeyException | DataLengthException
					| IllegalBlockSizeException | BadPaddingException | IllegalStateException
					| InvalidCipherTextException | IllegalArgumentException e) {
				cancel(new RollbackReason(this, "Could not decrypt file chunk. Reason: " + e.getMessage()));
				return;
			} catch (IOException e) {
				cancel(new RollbackReason(this, "Could not write file chunk. Reason: " + e.getMessage()));
				return;
			}
		}

		// all chunks downloaded
		if (chunkBuffer.isEmpty()) {
			// normal case: done with the process.
			logger.debug("Finished downloading file '" + destination + "'.");
		} else {
			// should be empty
			logger.error("All chunks downloaded but still some in buffer.");
			cancel(new RollbackReason(this, "Could not write all chunks to disk. We're stuck at chunk "
					+ currentChunkOrder));
		}
	}

	/**
	 * @return true when ok, otherwise false
	 * @throws InvalidProcessStateException
	 */
	private boolean verifyFile(File destination) throws InvalidProcessStateException {
		// verify before downloading
		if (destination != null && destination.exists()) {
			try {
				if (H2HEncryptionUtil.compareMD5(destination, context.getFileNode().getMD5())) {
					return false;
				} else {
					logger.warn("File already exists on disk, it will be overwritten");
				}
			} catch (IOException e) {
				// ignore and just downlaod the file
			}
		}

		return true;
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

					FileUtils.writeByteArrayToFile(destination, chunk.getData(), append);
					wroteToDisk.add(chunk);
					currentChunkOrder++;
				}
			}

			chunkBuffer.removeAll(wroteToDisk);
		} while (!wroteToDisk.isEmpty());
	}
}
