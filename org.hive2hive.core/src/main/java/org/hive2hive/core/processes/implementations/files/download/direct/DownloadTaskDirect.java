package org.hive2hive.core.processes.implementations.files.download.direct;

import java.io.File;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.model.Locations;
import org.hive2hive.core.model.MetaChunk;
import org.hive2hive.core.network.data.download.BaseDownloadTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DownloadTaskDirect extends BaseDownloadTask {

	private static final long serialVersionUID = 5219300641521251051L;
	private static final Logger logger = LoggerFactory.getLogger(DownloadTaskDirect.class);

	private final PublicKey fileKey; // the key of the file
	private final String ownUserName; // the user name of the downloader
	private final PeerAddress ownAddress; // the peer address of the downloader
	private final Set<String> users; // users having access to this file

	private transient CountDownLatch locationLocker;
	private volatile Set<Locations> locations;

	public DownloadTaskDirect(List<MetaChunk> metaChunks, File destination, PublicKey fileKey, String ownUserName,
			PeerAddress ownAddress, Set<String> users) {
		super(metaChunks, destination);
		this.fileKey = fileKey;
		this.ownUserName = ownUserName;
		this.ownAddress = ownAddress;
		this.users = users;
		this.locationLocker = new CountDownLatch(1);
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

	/**
	 * Returns a copy of the locations
	 * 
	 * @return
	 */
	public List<Locations> consumeLocationsBlocking() {
		try {
			locationLocker.await();
		} catch (InterruptedException e) {
			logger.warn("Could not wait until the locations are here");
		}

		return new ArrayList<Locations>(locations);
	}

	public synchronized void removeAddress(PeerAddress toRemove) {
		for (Locations location : locations) {
			location.removePeerAddress(toRemove);
		}
	}

	public void provideLocations(Set<Locations> locations) {
		this.locations = locations;
		locationLocker.countDown();
	}

	@Override
	public void reinitializeAfterDeserialization() {
		super.reinitializeAfterDeserialization();
		this.locationLocker = new CountDownLatch(1);
		this.locations = null;
	}
}
