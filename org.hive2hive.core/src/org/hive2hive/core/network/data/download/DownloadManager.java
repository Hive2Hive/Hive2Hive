package org.hive2hive.core.network.data.download;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.MetaChunk;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.network.data.download.dht.DownloadChunkDHT;
import org.hive2hive.core.network.data.download.dht.DownloadTaskDHT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DownloadManager {

	private final static Logger logger = LoggerFactory.getLogger(DownloadManager.class);

	private final IDataManager dataManager;
	private ExecutorService executor;
	private final Set<BaseDownloadTask> openTasks;

	public DownloadManager(IDataManager dataManager) {
		this.dataManager = dataManager;
		this.executor = Executors.newFixedThreadPool(H2HConstants.CONCURRENT_DOWNLOADS);
		this.openTasks = Collections.newSetFromMap(new ConcurrentHashMap<BaseDownloadTask, Boolean>());
	}

	public void submit(BaseDownloadTask task) {
		logger.debug("Submitted to download {}", task.getDestinationName());

		// store the task for possible later recovery
		openTasks.add(task);

		// add a listener
		task.addListener(new DownloadListener());

		// start the execution
		schedule(task);
	}

	private void schedule(BaseDownloadTask task) {
		// submit each chunk as a separate thread
		for (MetaChunk chunk : task.getOpenChunks()) {
			if (task.isDirectDownload()) {
				// TODO init the 'large' file runnable
			} else {
				DownloadChunkDHT runnable = new DownloadChunkDHT((DownloadTaskDHT) task, chunk, dataManager);
				executor.submit(runnable);
			}
		}
	}

	public void stopBackgroundProcesses() {
		executor.shutdownNow();
		while (!executor.isTerminated()) {
			logger.debug("Waiting for executor to shutdown...");
		}
	}

	public void continueBackgroundProcess() {
		executor = Executors.newFixedThreadPool(H2HConstants.CONCURRENT_DOWNLOADS);
		for (BaseDownloadTask task : openTasks) {
			schedule(task);
		}
	}

	public Set<BaseDownloadTask> getOpenTasks() {
		return openTasks;
	}

	/**
	 * Listens for a download to finish and removes it from the open list
	 */
	private class DownloadListener implements IDownloadListener {

		@Override
		public void downloadFinished(BaseDownloadTask task) {
			// remove it from the task list
			openTasks.remove(task);
			logger.debug("Task for downloading {} finished", task.getDestinationName());
		}

		@Override
		public void downloadFailed(BaseDownloadTask task, String reason) {
			// remove it from the task anyway
			openTasks.remove(task);
			logger.debug("Task for downloading {} failed", task.getDestinationName());
		}

	}
}
