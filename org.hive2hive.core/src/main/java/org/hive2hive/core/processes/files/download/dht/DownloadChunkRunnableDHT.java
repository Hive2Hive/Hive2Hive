package org.hive2hive.core.processes.files.download.dht;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.BaseNetworkContent;
import org.hive2hive.core.model.Chunk;
import org.hive2hive.core.model.MetaChunk;
import org.hive2hive.core.model.versioned.HybridEncryptedContent;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.parameters.IParameters;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.security.IH2HEncryption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Downloads a chunk from the DHT and stores it into a temprary file
 * 
 * @author Nico
 * 
 */
public class DownloadChunkRunnableDHT implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(DownloadChunkRunnableDHT.class);

	private final DownloadTaskDHT task;
	private final MetaChunk metaChunk;
	private final File tempDestination;
	private final DataManager dataManager;
	private final IH2HEncryption encryption;

	public DownloadChunkRunnableDHT(DownloadTaskDHT task, MetaChunk chunk, DataManager dataManager, IH2HEncryption encryption) {
		this.task = task;
		this.metaChunk = chunk;
		this.dataManager = dataManager;
		this.encryption = encryption;

		// create temporary file
		this.tempDestination = new File(task.getTempDirectory(), task.getDestinationName() + "-" + chunk.getIndex());
	}

	@Override
	public void run() {
		if (task.isAborted()) {
			logger.warn("Abort scheduled download of chunk {} of file {}", metaChunk.getIndex(), task.getDestinationName());
			return;
		} else if (Thread.currentThread().isInterrupted()) {
			logger.warn("Not terminate the download because thread is interrupted");
			return;
		}

		logger.debug("Downloading chunk {} of file {} from the DHT", metaChunk.getIndex(), task.getDestinationName());
		IParameters parameters = new Parameters().setLocationKey(metaChunk.getChunkId()).setContentKey(
				H2HConstants.FILE_CHUNK);
		BaseNetworkContent content = dataManager.get(parameters);
		if (content == null) {
			task.abortDownload("Chunk not found in the DHT");
			return;
		}

		HybridEncryptedContent encrypted = (HybridEncryptedContent) content;
		Chunk chunk;

		try {
			BaseNetworkContent decrypted = encryption.decryptHybrid(encrypted, task.getDecryptionKey());
			chunk = (Chunk) decrypted;
		} catch (GeneralSecurityException | IllegalArgumentException | IOException | ClassNotFoundException e) {
			task.abortDownload(String.format("Decryption of the chunk failed. reason = '%s'", e.getMessage()));
			return;
		}

		try {
			FileUtils.writeByteArrayToFile(tempDestination, chunk.getData());
		} catch (IOException e) {
			task.abortDownload("Cannot write the chunk data to temporary file");
			return;
		}

		// TODO verify MD5 hash here

		// notify the task that this file part has been downloaded successfully
		task.markDownloaded(metaChunk.getIndex(), tempDestination);
	}
}
