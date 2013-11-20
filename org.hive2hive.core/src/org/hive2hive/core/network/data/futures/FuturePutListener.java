package org.hive2hive.core.network.data.futures;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.FutureGet;
import net.tomp2p.futures.FuturePut;
import net.tomp2p.futures.FutureRemove;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.H2HStorageMemory.PutStatusH2H;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.IPutListener;
import org.hive2hive.core.network.data.NetworkContent;

public class FuturePutListener extends BaseFutureAdapter<FuturePut> {

	private final static Logger logger = H2HLoggerFactory.getLogger(FuturePutListener.class);

	private final String locationKey;
	private final String contentKey;
	private final NetworkContent content;
	private final IPutListener listener;
	private final NetworkManager networkManager;

	// used to count put retries
	private int putTries = 0;
	// used to count get tries
	private int getTries = 0;

	public FuturePutListener(String locationKey, String contentKey, NetworkContent content,
			IPutListener listener, NetworkManager networkManager) {
		this.locationKey = locationKey;
		this.contentKey = contentKey;
		this.content = content;
		this.listener = listener;
		this.networkManager = networkManager;
	}

	@Override
	public void operationComplete(FuturePut future) throws Exception {
		logger.debug(String.format(
				"Start verification of put. location key = '%s' content key = '%s' version key = '%s'",
				locationKey, contentKey, content.getVersionKey()));

		if (future.isFailed()) {
			logger.warn(String
					.format("Put future was not successful. location key = '%s' content key = '%s' version key = '%s'",
							locationKey, contentKey, content.getVersionKey()));
			retryPut();
			return;
		} else if (future.getRawResult().isEmpty()) {
			logger.warn("Returned raw results are empty.");
			retryPut();
			return;
		}

		// analyze returned put status
		Map<Number640, PeerAddress> versionConflict = new HashMap<Number640, PeerAddress>();
		Map<Number640, PeerAddress> fail = new HashMap<Number640, PeerAddress>();
		for (PeerAddress peeradress : future.getRawResult().keySet()) {
			for (Number640 key : future.getRawResult().get(peeradress).keySet()) {
				byte status = future.getRawResult().get(peeradress).get(key);
				switch (PutStatusH2H.values()[status]) {
					case OK:
						break;
					case FAILED:
					case FAILED_NOT_ABSENT:
					case FAILED_SECURITY:
						logger.warn(String
								.format("A node denied putting data. reason = '%s' location key = '%s' content key = '%s' version key = '%s'",
										PutStatusH2H.values()[status], locationKey, contentKey,
										content.getVersionKey()));
						fail.put(key, peeradress);
						break;
					case VERSION_CONFLICT:
					case VERSION_CONFLICT_NO_BASED_ON:
					case VERSION_CONFLICT_NO_VERSION_KEY:
					case VERSION_CONFLICT_OLD_TIMESTAMP:
						logger.warn(String
								.format("A version conflict detected. reason = '%s' location key = '%s' content key = '%s' version key = '%s'",
										PutStatusH2H.values()[status], locationKey, contentKey,
										content.getVersionKey()));
						versionConflict.put(key, peeradress);
						break;
				}
			}
		}

		if (!versionConflict.isEmpty()) {
			/*
			 * TODO check what is on other peers and which version is valid
			 * 1. contact this peers which indicate version conflict
			 * 2. get the version keys
			 * 3. compare it to own version key
			 * 4. determine who has newer one, this one wins the version conflict
			 * 4a. if necessary remove succeeded peers
			 * 5. verify with a delayed get
			 * 6. inform listener 
			 */
		} else if ((double) fail.size() < ((double) future.getRawResult().size()) / 2.0) {
			// majority of the contacted nodes responded with ok
			waitAMoment();
			verifyWithADelayedGet();
		} else {
			logger.warn(String.format("%s of %s contacted nodes failed.", fail.size(), future.getRawResult()
					.size()));
			retryPut();
		}
	}

	private void retryPut() {
		if (putTries++ < H2HConstants.PUT_RETRIES) {
			logger.warn(String.format(
					"Put retry #%s. location key = '%s' content key = '%s' version key = '%s'", putTries,
					locationKey, contentKey, content.getVersionKey()));
			// remove succeeded puts
			networkManager.getConnection().getPeer().remove(Number160.createHash(locationKey))
					.setContentKey(Number160.createHash(contentKey)).setVersionKey(content.getVersionKey())
					.start().addListener(new RetryPutListener());
		} else {
			logger.error(String
					.format("Put verification failed. Couldn't put data after %s tries. location key = '%s' content key = '%s' version key = '%s'",
							putTries - 1, locationKey, contentKey, content.getVersionKey()));
			if (listener != null)
				listener.onFailure();
		}
	}

	private class RetryPutListener extends BaseFutureAdapter<FutureRemove> {
		@Override
		public void operationComplete(FutureRemove future) throws Exception {
			if (future.isFailed())
				logger.warn(String
						.format("Put Retry: Could not delete the newly put content. location key = '%s' content key = '%s' version key = '%s'",
								locationKey, contentKey, content.getVersionKey()));
			try {
				Data data = new Data(content);
				data.ttlSeconds(content.getTimeToLive()).basedOn(content.getBasedOnKey());
				FuturePut futurePut = networkManager.getConnection().getPeer()
						.put(Number160.createHash(locationKey))
						.setData(Number160.createHash(contentKey), data)
						.setVersionKey(content.getVersionKey()).start();
				futurePut.addListener(FuturePutListener.this);
			} catch (IOException e) {
				logger.error(String.format("Exception while creating data object! exception = '%s'",
						e.getMessage()));
				if (listener != null)
					listener.onFailure();
			}
		}
	}

	private void checkVersionKey() {
		/*
		 * TODO check if version key is newest or contained in the history
		 * if newest in history --> my object is most recent one
		 * if not newest in history --> already newer version, but that's based on my version
		 * if not in list --> fork happened and I'm not part of the "right" branch. --> Rollback to base on
		 * newest version --> possibly throw an exception or repeat the put based on the most recent version
		 */
		boolean check = true;
		if (check) {
			logger.debug(String
					.format("Verification for put completed. location key = '%s' content key = '%s' version key = '%s'",
							locationKey, contentKey, content.getVersionKey()));
			// everything is ok
			if (listener != null)
				listener.onSuccess();
		} else {
			logger.warn(String
					.format("Put verification failed. Concurrent modification happened. location key = '%s' content key = '%s' version key = '%s'",
							locationKey, contentKey, content.getVersionKey()));
			if (listener != null)
				listener.onFailure();
		}
	}

	private void waitAMoment() {
		try {
			// wait a moment to give time to replicate the data
			Thread.sleep(H2HConstants.PUT_VERIFICATION_WAITING_TIME_MS);
		} catch (InterruptedException e) {
			logger.warn("Put verification woken up involuntarily.");
		}
	}

	private void verifyWithADelayedGet() {
		if (getTries++ < H2HConstants.GET_RETRIES) {
			// get data to verify if everything went correct
			FutureGet getFuture = networkManager.getConnection().getPeer()
					.get(Number160.createHash(locationKey)).setContentKey(Number160.createHash(contentKey))
					.start();
			getFuture.addListener(new DelayedGetListener());
		} else {
			logger.error(String
					.format("Put verification failed. Couldn't get data after %s tries. location key = '%s' content key = '%s'",
							getTries - 1, locationKey, contentKey));
			if (listener != null)
				listener.onFailure();
		}
	}

	private class DelayedGetListener extends BaseFutureAdapter<FutureGet> {
		@Override
		public void operationComplete(FutureGet future) throws Exception {
			if (future.isFailed() || future.getData() == null) {
				logger.warn(String
						.format("Put verification failed. Couldn't get data. Try #%s. location key = '%s' content key = '%s' version key = '%s'",
								getTries, locationKey, contentKey, content.getVersionKey()));
				verifyWithADelayedGet();
			} else {
				checkVersionKey();
			}
		}
	}
}
