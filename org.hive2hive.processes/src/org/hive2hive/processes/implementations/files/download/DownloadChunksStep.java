package org.hive2hive.processes.implementations.files.download;

import java.io.File;
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
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.Chunk;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.HybridEncryptedContent;
import org.hive2hive.processes.framework.RollbackReason;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.processes.implementations.common.base.BaseGetProcessStep;
import org.hive2hive.processes.implementations.context.DownloadFileContext;

public class DownloadChunksStep extends BaseGetProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(DownloadChunksStep.class);

	private final DownloadFileContext context;
	private final List<Chunk> chunkBuffer;
	private int currentChunkOrder;
	private Path destination;

	public DownloadChunksStep(DownloadFileContext context, NetworkManager networkManager) {
		super(networkManager);
		this.context = context;
		this.currentChunkOrder = 0;
		this.chunkBuffer = new ArrayList<Chunk>();
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		MetaFile metaFile = (MetaFile) context.consumeMetaDocument();

		// support to download a specific version
		int versionToDownload = context.getVersionToDownload();
		List<KeyPair> chunkKeys = metaFile.getNewestVersion().getChunkKeys();
		if (versionToDownload >= 0) {
			chunkKeys = metaFile.getVersionByIndex(versionToDownload).getChunkKeys();
		}

		// support to store the file on another location than default (used for recover)
		try {
			destination = networkManager.getSession().getFileManager().getPath(context.getFileNode());
			String fileName = context.getDestinationFileName();
			if (fileName != null) {
				destination = new File(destination.getParent().toFile(), fileName).toPath();
			}
		} catch (NoSessionException e) {
			cancel(new RollbackReason(this, "No session, thus the filemanager is missing"));
			return;
		}

		// verify before downloading
		File existing = destination.toFile();
		if (existing != null && existing.exists()) {
			try {
				if (H2HEncryptionUtil.compareMD5(existing, context.getFileNode().getMD5())) {
					cancel(new RollbackReason(this,
							"File already exists on disk. Content does match; no download needed"));
					return;
				} else {
					logger.warn("File already exists on disk, it will be overwritten");
				}
			} catch (IOException e) {
				// ignore and just downlaod the file
			}
		}

		// start the download
		int counter = 0;
		for (KeyPair chunkKey : chunkKeys) {
			logger.info("File " + destination + ": Downloading chunk " + counter++ + "/" + chunkKeys.size());
			NetworkContent content = get(H2HEncryptionUtil.key2String(chunkKey.getPublic()), H2HConstants.FILE_CHUNK);
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

					FileUtils.writeByteArrayToFile(destination.toFile(), chunk.getData(), append);
					wroteToDisk.add(chunk);
					currentChunkOrder++;
				}
			}

			chunkBuffer.removeAll(wroteToDisk);
		} while (!wroteToDisk.isEmpty());
	}
}
