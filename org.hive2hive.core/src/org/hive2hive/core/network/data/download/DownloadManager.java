package org.hive2hive.core.network.data.download;

import java.io.File;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.MetaChunk;
import org.hive2hive.core.network.data.IDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DownloadManager {

	private final static Logger logger = LoggerFactory.getLogger(DownloadManager.class);

	private final IDataManager dataManager;
	private ExecutorService executor;
	private final Set<DownloadTask> openTasks;

	public DownloadManager(IDataManager dataManager) {
		this.dataManager = dataManager;
		this.executor = Executors.newFixedThreadPool(H2HConstants.CONCURRENT_DOWNLOADS);
		this.openTasks = Collections.newSetFromMap(new ConcurrentHashMap<DownloadTask, Boolean>());
	}

	public void submit(DownloadTask task) {
		logger.debug("Submitted to download {}", task.getDestinationName());

		// store the task for possible later recovery
		openTasks.add(task);

		// add a listener
		task.addListener(new DownloadListener());

		// start the execution
		schedule(task);
	}

	private void schedule(DownloadTask task) {
		// submit each chunk as a separate thread
		for (MetaChunk chunk : task.getOpenChunks()) {
			File tempFile = new File(task.getTempDirectory(), task.getDestinationName() + "-"
					+ chunk.getIndex());
			DownloadSingleChunk runnable = new DownloadSingleChunk(task, chunk, tempFile, dataManager);
			executor.submit(runnable);
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
		for (DownloadTask task : openTasks) {
			schedule(task);
		}
	}

	/**
	 * Listens for a download to finish and removes it from the open list
	 */
	private class DownloadListener implements IDownloadListener {

		@Override
		public void downloadFinished(DownloadTask task) {
			// remove it from the task list
			openTasks.remove(task);
			logger.debug("Task for downloading {} finished", task.getDestinationName());
		}

		@Override
		public void downloadFailed(DownloadTask task, String reason) {
			// remove it from the task anyway
			openTasks.remove(task);
			logger.debug("Task for downloading {} failed", task.getDestinationName());
		}

	}
}
