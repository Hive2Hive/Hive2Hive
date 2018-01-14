package org.hive2hive.core.network.data.download;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.model.MetaChunk;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.processes.files.download.dht.DownloadChunkRunnableDHT;
import org.hive2hive.core.processes.files.download.dht.DownloadTaskDHT;
import org.hive2hive.core.processes.files.download.direct.DownloadChunkRunnableDirect;
import org.hive2hive.core.processes.files.download.direct.DownloadTaskDirect;
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

	private final NetworkManager networkManager;
	private final IFileConfiguration fileConfig;
	private final Set<BaseDownloadTask> openTasks;

	private ExecutorService executor;

	public DownloadManager(NetworkManager networkManager, IFileConfiguration fileConfig) {
		this.networkManager = networkManager;
		this.fileConfig = fileConfig;
		this.openTasks = Collections.newSetFromMap(new ConcurrentHashMap<BaseDownloadTask, Boolean>());
		// start executor
		this.executor = Executors.newFixedThreadPool(H2HConstants.CONCURRENT_DOWNLOADS);
	}

	/**
	 * Add a new task to download a file. The download is automatically started in the background
	 * 
	 * @param task the task to submit
	 * @throws NoPeerConnectionException if the peer is not connected
	 */
	public void submit(BaseDownloadTask task) throws NoPeerConnectionException {
		logger.debug("Submitted to download {}", task.getDestinationName());

		// store the task for possible later recovery
		openTasks.add(task);

		// add a listener
		task.addListener(new DownloadListener());

		// start the execution
		schedule(task);
	}

	private void schedule(BaseDownloadTask task) throws NoPeerConnectionException {
		if (task.isDirectDownload()) {
			// first get the locations of all users having access to this file
			DownloadTaskDirect directTask = (DownloadTaskDirect) task;
			directTask.startFetchLocations(networkManager.getDataManager());

			// then download all chunks in separate threads
			for (MetaChunk chunk : task.getOpenChunks()) {
				DownloadChunkRunnableDirect runnable = new DownloadChunkRunnableDirect(directTask, chunk,
						networkManager.getMessageManager(), fileConfig);
				executor.submit(runnable);
			}
		} else {
			// submit each chunk as a separate thread
			for (MetaChunk chunk : task.getOpenChunks()) {
				DownloadChunkRunnableDHT runnable = new DownloadChunkRunnableDHT((DownloadTaskDHT) task, chunk,
						networkManager.getDataManager(), networkManager.getEncryption());
				executor.submit(runnable);
			}
		}
	}

	/**
	 * Stop the downloads
	 */
	public void stopBackgroundProcesses() {
		executor.shutdownNow();
		logger.debug("All downloads stopped");
	}

	/**
	 * Start / continue the downloads
	 * 
	 * @throws NoPeerConnectionException if the peer is not connected
	 */
	public void startBackgroundProcess() throws NoPeerConnectionException {
		executor = Executors.newFixedThreadPool(H2HConstants.CONCURRENT_DOWNLOADS);
		for (BaseDownloadTask task : openTasks) {
			schedule(task);
		}
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
