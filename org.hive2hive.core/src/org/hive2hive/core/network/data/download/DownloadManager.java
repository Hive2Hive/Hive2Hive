package org.hive2hive.core.network.data.download;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.model.MetaChunk;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.network.data.PublicKeyManager;
import org.hive2hive.core.network.messages.IMessageManager;
import org.hive2hive.core.processes.implementations.files.download.dht.DownloadChunkRunnableDHT;
import org.hive2hive.core.processes.implementations.files.download.dht.DownloadTaskDHT;
import org.hive2hive.core.processes.implementations.files.download.direct.DownloadChunkRunnableDirect;
import org.hive2hive.core.processes.implementations.files.download.direct.DownloadTaskDirect;
import org.hive2hive.core.processes.implementations.files.download.direct.GetLocationsList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DownloadManager {

	private final static Logger logger = LoggerFactory.getLogger(DownloadManager.class);

	private final IDataManager dataManager;
	private final IMessageManager messageManager;
	private final PublicKeyManager keyManager;
	private final IFileConfiguration fileConfig;
	private final Set<BaseDownloadTask> openTasks;

	private ExecutorService executor;

	public DownloadManager(IDataManager dataManager, IMessageManager messageManager,
			PublicKeyManager keyManager, IFileConfiguration fileConfig) {
		this.dataManager = dataManager;
		this.messageManager = messageManager;
		this.keyManager = keyManager;
		this.fileConfig = fileConfig;
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
		if (task.isDirectDownload()) {
			// first get the locations of all users having access to this file
			DownloadTaskDirect directTask = (DownloadTaskDirect) task;
			directTask.resetLocations();
			// Hint: Run it in a separate thread (not in the thread pool) because the executor does not
			// guarantee the in-order processing.
			new Thread(new GetLocationsList(directTask, dataManager)).start();
		}

		// submit each chunk as a separate thread
		for (MetaChunk chunk : task.getOpenChunks()) {
			if (task.isDirectDownload()) {
				// then download all chunks
				DownloadChunkRunnableDirect runnable = new DownloadChunkRunnableDirect(
						(DownloadTaskDirect) task, chunk, messageManager, keyManager, fileConfig);
				executor.submit(runnable);
			} else {
				DownloadChunkRunnableDHT runnable = new DownloadChunkRunnableDHT((DownloadTaskDHT) task,
						chunk, dataManager);
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
