package org.hive2hive.core.processes.files.download;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.FileIndex;
import org.hive2hive.core.model.MetaChunk;
import org.hive2hive.core.model.versioned.BaseMetaFile;
import org.hive2hive.core.model.versioned.MetaFileLarge;
import org.hive2hive.core.model.versioned.MetaFileSmall;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.processes.context.DownloadFileContext;
import org.hive2hive.core.processes.files.download.dht.DownloadTaskDHT;
import org.hive2hive.core.processes.files.download.direct.DownloadTaskDirect;
import org.hive2hive.core.security.HashUtil;
import org.hive2hive.processframework.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nico, Seppi
 */
public class InitDownloadChunksStep extends ProcessStep<Void> {

	private static final Logger logger = LoggerFactory.getLogger(InitDownloadChunksStep.class);

	private final DownloadFileContext context;
	private final NetworkManager networkManager;

	private File destination;

	public InitDownloadChunksStep(DownloadFileContext context, NetworkManager networkManager) {
		this.setName(getClass().getName());
		this.context = context;
		this.networkManager = networkManager;
	}

	@Override
	protected Void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		BaseMetaFile metaFile = context.consumeMetaFile();

		H2HSession session;
		try {
			session = networkManager.getSession();
		} catch (NoSessionException ex) {
			throw new ProcessExecutionException(this, ex);
		}

		// support to store the file on another location than default (used for recovery)
		if (context.downloadToDefaultDestination()) {
			destination = context.consumeIndex().asFile(session.getRootFile());
		} else {
			destination = context.getDestination();
		}

		if (metaFile.isSmall()) {
			// download chunks from DHT
			MetaFileSmall metaFileSmall = (MetaFileSmall) metaFile;

			// support to download a specific version
			List<MetaChunk> metaChunks;
			if (context.downloadNewestVersion()) {
				metaChunks = metaFileSmall.getNewestVersion().getMetaChunks();
			} else {
				metaChunks = metaFileSmall.getVersionByIndex(context.getVersionToDownload()).getMetaChunks();
			}

			// verify destination before downloading
			if (destination != null && destination.exists()) {
				// can be cast because only files are downloaded
				FileIndex fileIndex = (FileIndex) context.consumeIndex();
				try {
					if (HashUtil.compare(destination, fileIndex.getMD5())) {
						throw new ProcessExecutionException(this, 
								"File already exists on disk. Content does match. No download needed.");
					}
				} catch (IOException ex) {
					throw new ProcessExecutionException(this, ex, "Got an unexpected exception while comparing hashes.");
				}
			}

			// start the download
			DownloadTaskDHT task = new DownloadTaskDHT(metaChunks, destination, metaFileSmall.getChunkKey().getPrivate(),
					networkManager.getEventBus());
			session.getDownloadManager().submit(task);

			// join the download process
			try {
				task.join(this);
			} catch (InterruptedException ex) {
				throw new ProcessExecutionException(this, ex);
			}
		} else {
			// download chunks from users
			MetaFileLarge metaFileLarge = (MetaFileLarge) metaFile;

			Set<String> users = context.consumeIndex().getCalculatedUserList();
			DownloadTaskDirect task = new DownloadTaskDirect(metaFileLarge.getMetaChunks(), destination, metaFile.getId(),
					session.getUserId(), networkManager.getConnection().getPeerDHT().peerAddress(), users,
					networkManager.getEventBus());
			session.getDownloadManager().submit(task);

			// join the download process
			try {
				task.join(this);
			} catch (InterruptedException ex) {
				throw new ProcessExecutionException(this, ex);
			}
		}
		logger.debug("Finished downloading file '{}'.", destination);
		
		return null;
	}

}
