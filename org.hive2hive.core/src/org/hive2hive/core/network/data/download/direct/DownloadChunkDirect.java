package org.hive2hive.core.network.data.download.direct;

import java.io.File;

import org.hive2hive.core.model.MetaChunk;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.network.data.download.BaseDownloadTask;
import org.hive2hive.core.network.messages.IMessageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Downloads a chunk from the DHT and stores it into a temprary file
 * 
 * @author Nico
 * 
 */
public class DownloadChunkDirect implements Runnable {

	private final static Logger logger = LoggerFactory.getLogger(DownloadChunkDirect.class);

	private final BaseDownloadTask task;
	private final MetaChunk metaChunk;
	private final File tempDestination;
	private final IDataManager dataManager;
	private final IMessageManager messageManager;

	public DownloadChunkDirect(BaseDownloadTask task, MetaChunk chunk, File tempDestination,
			IDataManager dataManager, IMessageManager messageManager) {
		this.task = task;
		this.metaChunk = chunk;
		this.tempDestination = tempDestination;
		this.dataManager = dataManager;
		this.messageManager = messageManager;
	}

	@Override
	public void run() {
		if (task.isAborted()) {
			logger.warn("Abort scheduled download of chunk {} of file {}", metaChunk.getIndex(),
					task.getDestinationName());
			return;
		}

		// TODO

		// notify the task that this file has been downloaded successfully
		task.setDownloaded(metaChunk.getIndex(), tempDestination);
		logger.debug("Successfully downloaded chunk {} of file {}", metaChunk.getIndex(),
				task.getDestinationName());
	}
}
