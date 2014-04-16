package org.hive2hive.core.processes.implementations.files.download;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.model.FileIndex;
import org.hive2hive.core.model.MetaChunk;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.model.MetaFileLarge;
import org.hive2hive.core.model.MetaFileSmall;
import org.hive2hive.core.network.data.download.DownloadTask;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.context.DownloadFileContext;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InitDownloadChunksStep extends ProcessStep {

	private final static Logger logger = LoggerFactory.getLogger(InitDownloadChunksStep.class);

	private final DownloadFileContext context;
	private final H2HSession session;
	private File destination;

	public InitDownloadChunksStep(DownloadFileContext context, H2HSession session) {
		this.context = context;
		this.session = session;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		MetaFile metaFile = context.consumeMetaFile();

		if (metaFile.isSmall()) {
			downloadChunksFromDHT((MetaFileSmall) metaFile);
		} else {
			downloadChunksFromUsers((MetaFileLarge) metaFile);
		}

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

		// support to store the file on another location than default (used for recovery)
		if (context.downloadToDefaultDestination()) {
			destination = FileUtil.getPath(session.getRoot(), context.consumeIndex()).toFile();
		} else {
			destination = context.getDestination();
		}

		if (!validateDestination()) {
			throw new ProcessExecutionException(
					"File already exists on disk. Content does match; no download needed.");
		}

		try {
			// start the download
			DownloadTask downloadTask = new DownloadTask(metaChunks, false, destination, metaFile
					.getChunkKey().getPrivate());
			session.getDownloadManager().submit(downloadTask);
			downloadTask.join();
		} catch (InterruptedException e) {
			throw new ProcessExecutionException(e.getMessage());
		}

		// all chunks downloaded
		logger.debug("Finished downloading file '{}'.", destination);
	}

	private void downloadChunksFromUsers(MetaFileLarge metaFile) {
		// TODO
		// set the destination to the default value
		// submit the task to a background thread that regularly checks for other clients, downloads and
		// assemblies the file
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
				if (H2HEncryptionUtil.compareMD5(destination, fileIndex.getMD5())) {
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
