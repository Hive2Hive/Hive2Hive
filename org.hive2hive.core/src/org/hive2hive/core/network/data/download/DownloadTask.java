package org.hive2hive.core.network.data.download;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.file.FileChunkUtil;
import org.hive2hive.core.model.MetaChunk;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DownloadTask implements Serializable {

	private static final long serialVersionUID = -6933011357191806148L;
	private final static Logger logger = LoggerFactory.getLogger(DownloadTask.class);

	private final List<MetaChunk> metaChunks;
	private final File destination;
	private final boolean directDownload;
	private final PrivateKey decryptionKey;
	private final File tempFolder;

	private final File[] downloadedParts;
	private final CountDownLatch latch;
	private final Set<IDownloadListener> listeners;
	private final AtomicBoolean aborted;
	private String reason;

	public DownloadTask(List<MetaChunk> metaChunks, boolean directDownload, File destination,
			PrivateKey decryptionKey) {
		this.metaChunks = metaChunks;
		this.directDownload = directDownload;
		this.destination = destination;
		this.decryptionKey = decryptionKey;
		this.downloadedParts = new File[metaChunks.size()];
		this.latch = new CountDownLatch(metaChunks.size());
		this.listeners = new HashSet<IDownloadListener>();
		this.aborted = new AtomicBoolean(false);

		// create the download folder
		String folderName = destination.getName() + "-" + UUID.randomUUID().toString();
		tempFolder = new File(FileUtils.getTempDirectory(), folderName);
		if (!tempFolder.mkdirs()) {
			logger.warn("Cannot create temporary download folder {}", tempFolder.getAbsolutePath());
		}
	}

	/**
	 * Returns a list of chunks that are not downloaded yed
	 * 
	 * @return
	 */
	public List<MetaChunk> getOpenChunks() {
		List<MetaChunk> openChunks = new ArrayList<MetaChunk>();
		for (MetaChunk metaChunk : metaChunks) {
			if (downloadedParts[metaChunk.getIndex()] == null) {
				openChunks.add(metaChunk);
			}
		}

		return openChunks;
	}

	public String getDestinationName() {
		return destination.getName();
	}

	public boolean isDirectDownload() {
		return directDownload;
	}

	public PrivateKey getDecryptionKey() {
		return decryptionKey;
	}

	public File getTempDirectory() {
		return tempFolder;
	}

	public void abortDownload(String reason) {
		this.reason = reason;
		aborted.set(true);

		// notify listeners
		for (IDownloadListener listener : listeners) {
			listener.downloadFailed(this, reason);
		}

		// immediately count down the latch
		while (latch.getCount() > 0)
			latch.countDown();
	}

	public boolean isAborted() {
		return aborted.get();
	}

	private boolean isDone() {
		for (int i = 0; i < downloadedParts.length; i++) {
			if (downloadedParts[i] == null)
				return false;
		}

		return true;
	}

	/**
	 * Mark the given chunk as downloaded
	 * 
	 * @param chunk
	 * @param filePart
	 */
	public synchronized void setDownloaded(int chunkIndex, File filePart) {
		downloadedParts[chunkIndex] = filePart;

		if (isDone()) {
			try {
				// reassembly
				List<File> fileParts = Arrays.asList(downloadedParts);
				FileChunkUtil.reassembly(fileParts, destination, true);

				// notify listeners
				for (IDownloadListener listener : listeners) {
					listener.downloadFinished(this);
				}
			} catch (IOException e) {
				abortDownload("Cannot reassembly the file parts");
			}
		}

		latch.countDown();
	}

	public void addListener(IDownloadListener listener) {
		listeners.add(listener);
	}

	/**
	 * Join the download process
	 * 
	 * @throws ProcessExecutionException if there was an error while downloading
	 * @throws InterruptedException if the process was interrupted or was unable to wait
	 */
	public void join() throws ProcessExecutionException, InterruptedException {
		latch.await();

		if (isAborted()) {
			throw new ProcessExecutionException(reason);
		} else if (!isDone()) {
			throw new InterruptedException("Could not wait until all downloads are done");
		}
	}
}
