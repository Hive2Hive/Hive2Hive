package org.hive2hive.core.processes.files.download.direct;

import java.io.File;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.events.EventBus;
import org.hive2hive.core.model.MetaChunk;
import org.hive2hive.core.model.versioned.Locations;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.PublicKeyManager;
import org.hive2hive.core.network.data.download.BaseDownloadTask;
import org.hive2hive.core.network.data.download.IDownloadListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DownloadTaskDirect extends BaseDownloadTask {

	private static final long serialVersionUID = 5219300641521251051L;
	private static final Logger logger = LoggerFactory.getLogger(DownloadTaskDirect.class);

	// the key of the file
	private final PublicKey fileKey;
	// the user name of the downloader
	private final String ownUserName;
	// the peer address of the downloader
	private final PeerAddress ownAddress;
	// users having access to this file
	private final Set<String> users;

	private final Set<Locations> locations;
	// is triggered as soon as the first locations are available
	private final CountDownLatch locationsLatch;

	public DownloadTaskDirect(List<MetaChunk> metaChunks, File destination, PublicKey fileKey, String ownUserName,
			PeerAddress ownAddress, Set<String> users, EventBus eventBus, PublicKeyManager keyManager) {
		super(metaChunks, destination, eventBus, keyManager);
		this.fileKey = fileKey;
		this.ownUserName = ownUserName;
		this.ownAddress = ownAddress;
		this.users = users;
		this.locations = Collections.synchronizedSet(new HashSet<Locations>());
		this.locationsLatch = new CountDownLatch(1);
	}

	@Override
	public boolean isDirectDownload() {
		return true;
	}

	public String getOwnUserName() {
		return ownUserName;
	}

	public PeerAddress getOwnAddress() {
		return ownAddress;
	}

	public PublicKey getFileKey() {
		return fileKey;
	}

	public Set<String> getUsers() {
		return users;
	}

	public void startFetchLocations(DataManager dataManager) {
		final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleAtFixedRate(new GetLocationsList(this, dataManager), 0, H2HConstants.DOWNLOAD_LOCATIONS_INTERVAL_S,
				TimeUnit.SECONDS);
		logger.debug("Started getting the locations for the downloads each {} seconds",
				H2HConstants.DOWNLOAD_LOCATIONS_INTERVAL_S);
		addListener(new IDownloadListener() {

			@Override
			public void downloadFinished(BaseDownloadTask task) {
				stop();
			}

			@Override
			public void downloadFailed(BaseDownloadTask task, String reason) {
				stop();
			}

			private void stop() {
				executor.shutdownNow();
				logger.debug("Stopped getting the locations regularly");
			}
		});
	}

	/**
	 * Returns a copy of the locations
	 * 
	 * @return a list of all available locations
	 */
	public List<Locations> getLocations() {
		synchronized (locations) {
			return new ArrayList<Locations>(locations);
		}
	}

	public synchronized void removeAddress(PeerAddress toRemove) {
		for (Locations location : locations) {
			location.removePeerAddress(toRemove);
		}
	}

	public boolean awaitLocations() {
		try {
			return locationsLatch.await(H2HConstants.DIRECT_DOWNLOAD_AWAIT_MS, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			return false;
		}
	}

	public void provideLocations(Set<Locations> locations) {
		synchronized (locations) {
			this.locations.clear();
			this.locations.addAll(locations);
		}
		locationsLatch.countDown();
	}
}
