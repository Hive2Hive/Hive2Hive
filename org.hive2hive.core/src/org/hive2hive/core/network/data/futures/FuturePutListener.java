package org.hive2hive.core.network.data.futures;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.CountDownLatch;

import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.FutureDigest;
import net.tomp2p.futures.FuturePut;
import net.tomp2p.futures.FutureRemove;
import net.tomp2p.p2p.builder.DigestBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.DigestResult;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.H2HStorageMemory.PutStatusH2H;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.NetworkContent;

/**
 * A put future adapter for verifying a put of a {@link NetworkContent} object. Provides failure handling and
 * a blocking wait.</br></br>
 * 
 * <b>Failure Handling</b></br>
 * Putting can fail when the future object failed, when the future object contains wrong data or the
 * responding node detected a failure. See {@link PutStatusH2H} for possible failures. If putting fails the
 * adapter retries it to a certain threshold (see {@link H2HConstants.PUT_RETRIES}). After a successful put
 * the adapter verifies with a digest if no concurrent modification happened. All puts are asynchronous.
 * That's why the future listener attaches himself to the new future objects so that the adapter can finally
 * notify his/her listener about a success or failure.
 * 
 * @author Seppi, Nico
 */
public class FuturePutListener extends BaseFutureAdapter<FuturePut> {

	private final static Logger logger = H2HLoggerFactory.getLogger(FuturePutListener.class);

	private final Number160 locationKey;
	private final Number160 domainKey;
	private final Number160 contentKey;
	private final KeyPair protectionKey;
	private final NetworkContent content;
	private final DataManager dataManager;
	private final CountDownLatch latch;

	// used to count put retries
	private int putTries = 0;
	private boolean success = false;

	public FuturePutListener(Number160 locationKey, Number160 domainKey, Number160 contentKey,
			NetworkContent content, KeyPair protectionKey, DataManager dataManager) {
		this.locationKey = locationKey;
		this.domainKey = domainKey;
		this.contentKey = contentKey;
		this.protectionKey = protectionKey;
		this.content = content;
		this.dataManager = dataManager;
		this.latch = new CountDownLatch(1);
	}

	/**
	 * Wait (blocking) until the put is done
	 * 
	 * @return true if successful, false if not successful
	 */
	public boolean await() {
		try {
			latch.await();
		} catch (InterruptedException e) {
			logger.error("Could not wait until put has finished", e);
		}

		return success;
	}

	@Override
	public void operationComplete(FuturePut future) throws Exception {
		logger.trace(String
				.format("Start verification of put. location key = '%s' domain key = '%s' content key = '%s' version key = '%s'",
						locationKey, domainKey, contentKey, content.getVersionKey()));

		if (future.isFailed()) {
			logger.warn(String
					.format("Put future was not successful. location key = '%s' domain key = '%s' content key = '%s' version key = '%s'",
							locationKey, domainKey, contentKey, content.getVersionKey()));
			retryPut();
			return;
		} else if (future.getRawResult().isEmpty()) {
			logger.warn("Returned raw results are empty.");
			retryPut();
			return;
		}

		// analyze returned put status
		final List<PeerAddress> versionConflict = new ArrayList<PeerAddress>();
		List<PeerAddress> fail = new ArrayList<PeerAddress>();
		for (PeerAddress peeradress : future.getRawResult().keySet()) {
			Map<Number640, Byte> map = future.getRawResult().get(peeradress);
			if (map == null) {
				logger.warn(String.format("A node gave no status (null) back."
						+ " location key = '%s' domain key = '%s' content key = '%s' version key = '%s'",
						locationKey, domainKey, contentKey, content.getVersionKey()));
				fail.add(peeradress);
			} else {
				for (Number640 key : future.getRawResult().get(peeradress).keySet()) {
					byte status = future.getRawResult().get(peeradress).get(key);
					switch (PutStatusH2H.values()[status]) {
						case OK:
							break;
						case FAILED:
						case FAILED_NOT_ABSENT:
						case FAILED_SECURITY:
							logger.warn(String
									.format("A node denied putting data. reason = '%s'"
											+ " location key = '%s' domain key = '%s' content key = '%s' version key = '%s'",
											PutStatusH2H.values()[status], locationKey, domainKey,
											contentKey, content.getVersionKey()));
							fail.add(peeradress);
							break;
						case VERSION_CONFLICT:
						case VERSION_CONFLICT_NO_BASED_ON:
						case VERSION_CONFLICT_NO_VERSION_KEY:
						case VERSION_CONFLICT_OLD_TIMESTAMP:
							logger.warn(String
									.format("A version conflict detected. reason = '%s'"
											+ " location key = '%s' domain key = '%s' content key = '%s' version key = '%s'",
											PutStatusH2H.values()[status], locationKey, domainKey,
											contentKey, content.getVersionKey()));
							versionConflict.add(peeradress);
							break;
					}
				}
			}
		}

		if (!versionConflict.isEmpty()) {
			logger.warn(String.format("Put verification failed. Version conflict!"
					+ " location key = '%s' domain key = '%s' content key = '%s' version key = '%s'",
					locationKey, domainKey, contentKey, content.getVersionKey()));
			notifyFailure();
		} else if ((double) fail.size() < ((double) future.getRawResult().size()) / 2.0) {
			// majority of the contacted nodes responded with ok
			verifyPut();
		} else {
			logger.warn(String.format("%s of %s contacted nodes failed.", fail.size(), future.getRawResult()
					.size()));
			retryPut();
		}
	}

	/**
	 * Retries a put till a certain threshold is reached (see {@link H2HConstants.PUT_RETRIES}). Removes first
	 * the possibly succeeded puts. A {@link RetryPutListener} tries to put again the given content.
	 */
	private void retryPut() {
		if (putTries++ < H2HConstants.PUT_RETRIES) {
			logger.warn(String
					.format("Put retry #%s. location key = '%s' domain key = '%s' content key = '%s' version key = '%s'",
							putTries, locationKey, domainKey, contentKey, content.getVersionKey()));
			// remove succeeded puts
			FutureRemove futureRemove = dataManager.remove(locationKey, domainKey, contentKey,
					content.getVersionKey(), protectionKey);
			futureRemove.addListener(new BaseFutureAdapter<FutureRemove>() {
				@Override
				public void operationComplete(FutureRemove future) {
					if (future.isFailed())
						logger.warn(String
								.format("Put Retry: Could not delete the newly put content."
										+ " location key = '%s' domain key = '%s' content key = '%s' version key = '%s'",
										locationKey, domainKey, contentKey, content.getVersionKey()));

					dataManager.put(locationKey, domainKey, contentKey, content, protectionKey).addListener(
							FuturePutListener.this);
				}
			});
		} else {
			logger.error(String.format("Put verification failed. Couldn't put data after %s tries."
					+ " location key = '%s' domain key = '%s' content key = '%s' version key = '%s'",
					putTries, locationKey, domainKey, contentKey, content.getVersionKey()));
			notifyFailure();
		}
	}

	/**
	 * Get the digest under the given location, domain and content key. Digest contains all version keys.
	 * 
	 * @return future with the digest request
	 */
	private FutureDigest getDigest() {
		DigestBuilder digestBuilder = dataManager.getDigest(locationKey);
		digestBuilder.from(new Number640(locationKey, domainKey, contentKey, Number160.ZERO)).to(
				new Number640(locationKey, domainKey, contentKey, Number160.MAX_VALUE));
		return digestBuilder.start();
	}

	/**
	 * Loads digest and triggers a check.
	 */
	private void verifyPut() {
		// get data to verify if everything went correct
		FutureDigest digestFuture = getDigest();
		digestFuture.addListener(new BaseFutureAdapter<FutureDigest>() {
			@Override
			public void operationComplete(FutureDigest future) throws Exception {
				if (future.isFailed() || future.getRawDigest() == null || future.getRawDigest().isEmpty()) {
					logger.error(String.format("Put verification failed. Couldn't get digest."
							+ " location key = '%s' domain key = '%s' content key = '%s' version key = '%s'",
							locationKey, domainKey, contentKey, content.getVersionKey()));
					notifyFailure();
				} else {
					checkVersionKey(future.getRawDigest());
				}
			}
		});
	}

	/**
	 * Checks if newly put version is listened in the digest. If yes everything is fine. If one peer doesn't
	 * contain the new version key it is a sign for a concurrent modification. In this case we have to figure
	 * out which newly put version wins.
	 * 
	 * @param rawDigest
	 *            raw digest data set
	 */
	private void checkVersionKey(Map<PeerAddress, DigestResult> rawDigest) {
		for (PeerAddress peerAddress : rawDigest.keySet()) {
			if (rawDigest.get(peerAddress) == null || rawDigest.get(peerAddress).getKeyDigest() == null
					|| rawDigest.get(peerAddress).getKeyDigest().isEmpty()) {
				logger.warn(String.format("Put verification: Received from peer '%s' no digest."
						+ " location key = '%s' domain key = '%s' content key = '%s' version key = '%s'",
						peerAddress, locationKey, domainKey, contentKey, content.getVersionKey()));
			} else {
				NavigableMap<Number640, Number160> keyDigest = rawDigest.get(peerAddress).getKeyDigest();

				if (keyDigest.firstEntry().getKey().getVersionKey().equals(content.getVersionKey())) {
					logger.trace(String.format("Put verification: On peer '%s' entry is newest."
							+ " location key = '%s' domain key = '%s' content key = '%s' version key = '%s'",
							peerAddress, locationKey, domainKey, contentKey, content.getVersionKey()));

				} else if (keyDigest.containsKey(new Number640(locationKey, domainKey, contentKey, content
						.getVersionKey()))) {
					logger.trace(String.format("Put verification: entry on peer '%s' exists in history."
							+ " location key = '%s' domain key = '%s' content key = '%s' version key = '%s'",
							peerAddress, locationKey, domainKey, contentKey, content.getVersionKey()));

				} else {
					logger.warn(String
							.format("Put verification: Concurrent modification on peer '%s' happened."
									+ " location key = '%s' domain key = '%s' content key = '%s' version key = '%s'",
									peerAddress, locationKey, domainKey, contentKey, content.getVersionKey()));

					// if version key is older than the other, the version wins
					if (!checkIfMyVerisonWins(keyDigest, peerAddress)) {
						notifyFailure();
						return;
					}
				}
			}
		}
		notifySuccess();
	}

	/**
	 * Checks if the new version key is older than the one listened in the digest.
	 * 
	 * @param keyDigest
	 *            digest of a peer
	 * @param peerAddress
	 *            the owner of the digest
	 * @return
	 *         <code>true</code> if the new version key has precedence to the one listened in the digest,
	 *         otherwise <code>false</code>
	 */
	protected boolean checkIfMyVerisonWins(NavigableMap<Number640, Number160> keyDigest,
			PeerAddress peerAddress) {
		/* Check if based on entry exists */
		if (!keyDigest
				.containsKey(new Number640(locationKey, domainKey, contentKey, content.getBasedOnKey()))) {
			logger.warn(String.format("Put verification: Peer '%s' doesn't contain based on version."
					+ " location key = '%s' domain key = '%s' content key = '%s' version key = '%s'",
					peerAddress, locationKey, domainKey, contentKey, content.getVersionKey()));
			// something is definitely wrong with this peer
			return true;
		} else {
			// figure out the next version based on same version
			Number640 entryBasingOnSameParent = getSuccessor(keyDigest);
			if (entryBasingOnSameParent == null) {
				if (keyDigest.firstKey().getVersionKey().equals(content.getBasedOnKey())) {
					logger.error(String.format("Put verification: Peer '%s' has no successor version."
							+ " location key = '%s' domain key = '%s' content key = '%s' version key = '%s'",
							peerAddress, locationKey, domainKey, contentKey, content.getVersionKey()));
					// this peer doesn't contain any successor version, with this peer is something wrong
					return true;
				} else {
					logger.error(String.format("Put verification: Peer '%s' has a corrupt version history."
							+ " location key = '%s' domain key = '%s' content key = '%s' version key = '%s'",
							peerAddress, locationKey, domainKey, contentKey, content.getVersionKey()));
					return true;
				}
			} else {
				int compare = entryBasingOnSameParent.getVersionKey().compareTo(content.getVersionKey());
				if (compare == 0) {
					logger.error(String.format("Put verification: Peer '%s' has same version."
							+ " location key = '%s' domain key = '%s' content key = '%s' version key = '%s'",
							peerAddress, locationKey, domainKey, contentKey, content.getVersionKey()));
					return true;
				} else if (compare < 0) {
					logger.warn(String.format("Put verification: Peer '%s' has older version."
							+ " location key = '%s' domain key = '%s' content key = '%s' version key = '%s'",
							peerAddress, locationKey, domainKey, contentKey, content.getVersionKey()));
					return false;
				} else {
					logger.warn(String.format("Put verification: Peer '%s' has newer version."
							+ " location key = '%s' domain key = '%s' content key = '%s' version key = '%s'",
							peerAddress, locationKey, domainKey, contentKey, content.getVersionKey()));
					return true;
				}
			}
		}
	}

	/**
	 * Get the entry which is the parent of the new version.
	 * 
	 * @param keyDigest
	 *            a digest containing the parent version (based on)
	 * @return the parent of the new version
	 */
	private Number640 getSuccessor(NavigableMap<Number640, Number160> keyDigest) {
		Number640 entryBasingOnSameParent = null;
		for (Number640 key : keyDigest.keySet()) {
			if (keyDigest.get(key).equals(content.getBasedOnKey())) {
				entryBasingOnSameParent = key;
				break;
			}
		}
		return entryBasingOnSameParent;
	}

	private void notifySuccess() {
		logger.trace(String
				.format("Verification for put completed. location key = '%s' domain key = '%s' content key = '%s' version key = '%s'",
						locationKey, domainKey, contentKey, content.getVersionKey()));
		// everything is ok
		success = true;
		latch.countDown();
	}

	/**
	 * Remove first potentially successful puts. Then notify the listener about the fail.
	 */
	private void notifyFailure() {
		// remove succeeded puts
		FutureRemove futureRemove = dataManager.remove(locationKey, domainKey, contentKey,
				content.getVersionKey(), protectionKey);
		futureRemove.addListener(new BaseFutureAdapter<FutureRemove>() {
			@Override
			public void operationComplete(FutureRemove future) {
				if (future.isFailed())
					logger.warn(String.format("Put Retry: Could not delete the newly put content."
							+ " location key = '%s' domain key = '%s' content key = '%s' version key = '%s'",
							locationKey, domainKey, contentKey, content.getVersionKey()));

				success = false;
				latch.countDown();
			}
		});
	}
}
