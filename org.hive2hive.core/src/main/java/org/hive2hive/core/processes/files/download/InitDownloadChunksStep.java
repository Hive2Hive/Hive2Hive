package org.hive2hive.core.processes.files.download;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.model.FileIndex;
import org.hive2hive.core.model.MetaChunk;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.model.MetaFileLarge;
import org.hive2hive.core.model.MetaFileSmall;
import org.hive2hive.core.processes.context.DownloadFileContext;
import org.hive2hive.core.processes.files.download.dht.DownloadTaskDHT;
import org.hive2hive.core.processes.files.download.direct.DownloadTaskDirect;
import org.hive2hive.core.security.HashUtil;
import org.hive2hive.processframework.abstracts.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InitDownloadChunksStep extends ProcessStep {

	private static final Logger logger = LoggerFactory.getLogger(InitDownloadChunksStep.class);

	private final DownloadFileContext context;
	private final H2HSession session;
	private final PeerAddress ownPeerAddress;

	private File destination;

	public InitDownloadChunksStep(DownloadFileContext context, H2HSession session, PeerAddress ownPeerAddress) {
		this.context = context;
		this.session = session;
		this.ownPeerAddress = ownPeerAddress;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		MetaFile metaFile = context.consumeMetaFile();

		// support to store the file on another location than default (used for recovery)
		if (context.downloadToDefaultDestination()) {
			destination = FileUtil.getPath(session.getRoot(), context.consumeIndex()).toFile();
		} else {
			destination = context.getDestination();
		}

		if (metaFile.isSmall()) {
			downloadChunksFromDHT((MetaFileSmall) metaFile);
		} else {
			downloadChunksFromUsers((MetaFileLarge) metaFile);
		}

		logger.debug("Finished downloading file '{}'.", destination);
	}

	private void downloadChunksFromDHT(MetaFileSmall metaFile) throws InvalidProcessStateException,
			ProcessExecutionException {
		// support to download a specific version
		List<MetaChunk> metaChunks;
		if (context.downloadNewestVersion()) {
			metaChunks = metaFile.getNewestVersion().getMetaChunks();
		} else {
			metaChunks = metaFile.getVersionByIndex(context.getVersionToDownload()).getMetaChunks();
		}

		if (!validateDestination()) {
			throw new ProcessExecutionException("File already exists on disk. Content does match; no download needed.");
		}

		try {
			// start the download
			DownloadTaskDHT task = new DownloadTaskDHT(metaChunks, destination, metaFile.getChunkKey().getPrivate());
			session.getDownloadManager().submit(task);
			task.join();
		} catch (InterruptedException e) {
			throw new ProcessExecutionException(e.getMessage());
		}
	}

	private void downloadChunksFromUsers(MetaFileLarge metaFile) throws ProcessExecutionException {
		// TODO support versioning at large files as well

		try {
			Set<String> users = context.consumeIndex().getCalculatedUserList();
			DownloadTaskDirect task = new DownloadTaskDirect(metaFile.getMetaChunks(), destination, metaFile.getId(),
					session.getUserId(), ownPeerAddress, users);
			session.getDownloadManager().submit(task);
			task.join();
		} catch (InterruptedException e) {
			throw new ProcessExecutionException(e.getMessage());
		}

	}

	/**
	 * @return true when ok, otherwise false
	 * @throws InvalidProcessStateException
	 */
	private boolean validateDestination() throws InvalidProcessStateException {
		// verify before downloading
		if (destination != null && destination.exists()) {
			try {
				// can be cast because only files are downloaded
				FileIndex fileIndex = (FileIndex) context.consumeIndex();
				if (HashUtil.compare(destination, fileIndex.getMD5())) {
					return false;
				} else {
					logger.warn("File already exists on disk. It will be overwritten.");
				}
			} catch (IOException e) {
				// ignore and just download the file
			}
		}

		return true;
	}
}
