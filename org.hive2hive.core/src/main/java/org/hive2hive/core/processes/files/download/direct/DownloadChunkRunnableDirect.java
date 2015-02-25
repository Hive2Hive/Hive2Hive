package org.hive2hive.core.processes.files.download.direct;

import java.io.File;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.model.MetaChunk;
import org.hive2hive.core.network.messages.IMessageManager;
import org.hive2hive.core.processes.files.download.direct.process.AskForChunkStep;
import org.hive2hive.core.processes.files.download.direct.process.DownloadDirectContext;
import org.hive2hive.core.processes.files.download.direct.process.SelectPeerForDownloadStep;
import org.hive2hive.processframework.composites.SyncProcess;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Downloads a chunk from the DHT and stores it into a temporary file
 * 
 * @author Nico
 * 
 */
public class DownloadChunkRunnableDirect implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(DownloadChunkRunnableDirect.class);

	private final DownloadTaskDirect task;
	private final MetaChunk metaChunk;
	private final File tempDestination;
	private final IMessageManager messageManager;
	private final IFileConfiguration config;

	public DownloadChunkRunnableDirect(DownloadTaskDirect task, MetaChunk metaChunk, IMessageManager messageManager,
			IFileConfiguration config) {
		this.task = task;
		this.metaChunk = metaChunk;
		this.messageManager = messageManager;
		this.config = config;

		// create temporary file
		this.tempDestination = new File(task.getTempDirectory(), task.getDestinationName() + "-" + metaChunk.getIndex());
	}

	@Override
	public void run() {
		if (task.awaitLocations()) {
			logger.debug("Locations are available and download can be started");
		} else {
			logger.warn("Locations are not available, abort download");
			task.abortDownload("Locations are not available in reasonable time");
			return;
		}

		int currentTry = 0;
		while (task.getOpenChunks().contains(metaChunk)) {
			if (task.isAborted()) {
				logger.warn("Abort scheduled download of chunk {} of file {}", metaChunk.getIndex(),
						task.getDestinationName());
				return;
			} else if (Thread.currentThread().isInterrupted()) {
				logger.warn("Not terminate the download because thread is interrupted");
				return;
			} else if (currentTry >= H2HConstants.MAX_RETRIES_DOWNLOAD_SAME_CHUNK) {
				logger.error("Downloading chunk with index {} was retried {} times. Will stop the download now",
						metaChunk.getIndex(), currentTry);
				task.abortDownload("Retry count for chunk " + metaChunk.getIndex() + " exceeded the limit");
				return;
			}

			currentTry++;

			DownloadDirectContext context = new DownloadDirectContext(task, metaChunk, tempDestination);
			SyncProcess process = new SyncProcess();
			process.add(new SelectPeerForDownloadStep(context));
			process.add(new AskForChunkStep(context, messageManager, config));

			try {
				process.execute();
				logger.debug("Successfully downloaded meta chunk {}", metaChunk.getIndex());
			} catch (InvalidProcessStateException | ProcessExecutionException ex) {
				logger.warn("Downloading chunk {} failed ({})", metaChunk.getIndex(), currentTry);
			}
		}
	}
}
