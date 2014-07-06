package org.hive2hive.core.network.data.download;

import java.io.File;
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
import org.hive2hive.core.processes.files.download.dht.DownloadChunkRunnableDHT;
import org.hive2hive.core.processes.files.download.dht.DownloadTaskDHT;
import org.hive2hive.core.processes.files.download.direct.DownloadChunkRunnableDirect;
import org.hive2hive.core.processes.files.download.direct.DownloadTaskDirect;
import org.hive2hive.core.processes.files.download.direct.GetLocationsList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A download manager handling downloads. Downloading chunks happens concurrently. It is possible to download
 * multiple files at a time. The number of concurrent downloads is configurable over the
 * {@link H2HConstants#CONCURRENT_DOWNLOADS} field. <br>
 * Downloaded chunks are stored in a temporary folder and assembled when all chunks are downloaded.
 * 
 * @author Nico
 * 
 */
public class DownloadManager {

	private static final Logger logger = LoggerFactory.getLogger(DownloadManager.class);

	private final IDataManager dataManager;
	private final IMessageManager messageManager;
	private final PublicKeyManager keyManager;
	private final IFileConfiguration fileConfig;
	private final Set<BaseDownloadTask> openTasks;

	private ExecutorService executor;

	public DownloadManager(IDataManager dataManager, IMessageManager messageManager, PublicKeyManager keyManager,
			IFileConfiguration fileConfig) {
		this.dataManager = dataManager;
		this.messageManager = messageManager;
		this.keyManager = keyManager;
		this.fileConfig = fileConfig;
		this.executor = Executors.newFixedThreadPool(H2HConstants.CONCURRENT_DOWNLOADS);
		this.openTasks = Collections.newSetFromMap(new ConcurrentHashMap<BaseDownloadTask, Boolean>());
	}

	/**
	 * Add a new task to download a file. The download is automatically started in the background
	 */
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
			// Hint: Run it in a separate thread (not in the thread pool) because the executor does not
			// guarantee the in-order processing.
			new Thread(new GetLocationsList(directTask, dataManager)).start();

			// then download all chunks in separate threads
			for (MetaChunk chunk : task.getOpenChunks()) {
				DownloadChunkRunnableDirect runnable = new DownloadChunkRunnableDirect(directTask, chunk, messageManager,
						keyManager, fileConfig);
				executor.submit(runnable);
			}
		} else {
			// submit each chunk as a separate thread
			for (MetaChunk chunk : task.getOpenChunks()) {
				DownloadChunkRunnableDHT runnable = new DownloadChunkRunnableDHT((DownloadTaskDHT) task, chunk, dataManager);
				executor.submit(runnable);
			}
		}
	}

	/**
	 * Stop the downloads
	 */
	public void stopBackgroundProcesses() {
		executor.shutdownNow();
		while (!executor.isTerminated()) {
			logger.debug("Waiting for executor to shutdown...");
		}
	}

	/**
	 * Continue with the downloads
	 */
	public void continueBackgroundProcess() {
		executor = Executors.newFixedThreadPool(H2HConstants.CONCURRENT_DOWNLOADS);
		for (BaseDownloadTask task : openTasks) {
			schedule(task);
		}
	}

	/**
	 * Return the task which are currently downloading or waiting for a download slot
	 */
	public Set<BaseDownloadTask> getOpenTasks() {
		return openTasks;
	}

	/**
	 * Indicates whether the download manager has already a task to download the given file
	 */
	public boolean isDownloading(File file) {
		for (BaseDownloadTask openTask : openTasks) {
			if (openTask.getDestination().equals(file)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Listens for a download to finish and removes it from the open list
	 */
	private class DownloadListener implements IDownloadListener {

		@Override
		public void downloadFinished(BaseDownloadTask task) {
			// remove it from the task list
			openTasks.remove(task);
			logger.debug("Task for downloading '{}' finished.", task.getDestinationName());
		}

		@Override
		public void downloadFailed(BaseDownloadTask task, String reason) {
			// remove it from the task anyway
			openTasks.remove(task);
			logger.debug("Task for downloading '{}' failed.", task.getDestinationName());
		}

	}
}
