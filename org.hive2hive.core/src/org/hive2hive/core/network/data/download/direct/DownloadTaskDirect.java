package org.hive2hive.core.network.data.download.direct;

import java.io.File;
import java.security.PrivateKey;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.hive2hive.core.model.Locations;
import org.hive2hive.core.model.MetaChunk;
import org.hive2hive.core.network.data.download.BaseDownloadTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DownloadTaskDirect extends BaseDownloadTask {

	private static final long serialVersionUID = 5219300641521251051L;
	private final static Logger logger = LoggerFactory.getLogger(DownloadTaskDirect.class);

	private final CountDownLatch locationLocker;
	private Set<Locations> locations;

	public DownloadTaskDirect(List<MetaChunk> metaChunks, File destination, PrivateKey decryptionKey) {
		super(metaChunks, destination, decryptionKey);
		this.locationLocker = new CountDownLatch(1);
	}

	@Override
	public boolean isDirectDownload() {
		return true;
	}

	public Set<Locations> consumeLocationsBlocking() {
		try {
			locationLocker.await();
		} catch (InterruptedException e) {
			logger.warn("Could not wait until the locations are here");
		}

		return locations;
	}

	public void provideLocations(Set<Locations> locations) {
		this.locations = locations;
		locationLocker.countDown();
	}
}
