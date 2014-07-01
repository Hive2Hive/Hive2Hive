package org.hive2hive.core.network.data.futures;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.CountDownLatch;

import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.FutureDigest;
import net.tomp2p.futures.FuturePut;
import net.tomp2p.futures.FutureRemove;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.DigestResult;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.NetworkContent;
import org.hive2hive.core.network.H2HStorageMemory.PutStatusH2H;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.parameters.IParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static final Logger logger = LoggerFactory.getLogger(FuturePutListener.class);

	private final IParameters parameters;
	private final DataManager dataManager;
	private final CountDownLatch latch;

	// used to count put retries
	private int putTries = 0;
	private boolean success = false;

	public FuturePutListener(IParameters parameters, DataManager dataManager) {
		this.parameters = parameters;
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
			logger.error("Could not wait until put has finished.", e);
		}

		return success;
	}

	@Override
	public void operationComplete(FuturePut future) throws Exception {
		logger.trace("Start verification of put. '{}'", parameters.toString());

		if (future.isFailed()) {
			logger.warn("Put future was not successful. '{}'", parameters.toString());
			retryPut();
			return;
		} else if (future.getRawResult().isEmpty()) {
			logger.warn("Returned raw results are empty.");
			retryPut();
			return;
		}

		// analyze returned put status
		final List<PeerAddress> versionConflict = new ArrayList<PeerAddress>();
		final List<PeerAddress> fail = new ArrayList<PeerAddress>();
		for (PeerAddress peeradress : future.getRawResult().keySet()) {
			Map<Number640, Byte> map = future.getRawResult().get(peeradress);
			if (map == null) {
				logger.warn("A node gave no status (null) back. '{}'", parameters.toString());
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
							logger.warn("A node denied putting data. Reason = '{}'. '{}'", PutStatusH2H.values()[status],
									parameters.toString());
							fail.add(peeradress);
							break;
						case VERSION_CONFLICT:
						case VERSION_CONFLICT_NO_BASED_ON:
						case VERSION_CONFLICT_NO_VERSION_KEY:
						case VERSION_CONFLICT_OLD_TIMESTAMP:
							logger.warn("Version conflict detected. Reason = '{}'. '{}'", PutStatusH2H.values()[status],
									parameters.toString());
							versionConflict.add(peeradress);
							break;
						default:
							logger.warn("Got an unknown status: {}", PutStatusH2H.values()[status]);
					}
				}
			}
		}

		if (!versionConflict.isEmpty()) {
			logger.warn("Put verification failed. Version conflict! '{}'", parameters.toString());
			notifyFailure();
		} else if ((double) fail.size() < ((double) future.getRawResult().size()) / 2.0) {
			// majority of the contacted nodes responded with ok
			verifyPut();
		} else {
			logger.warn("{} of {} contacted nodes failed.", fail.size(), future.getRawResult().size());
			retryPut();
		}
	}

	/**
	 * Retries a put till a certain threshold is reached (see {@link H2HConstants.PUT_RETRIES}). Removes first
	 * the possibly succeeded puts. A {@link RetryPutListener} tries to put again the given content.
	 */
	private void retryPut() {
		if (putTries++ < H2HConstants.PUT_RETRIES) {
			logger.warn("Put retry #{}. '{}'", putTries, parameters.toString());
			// remove succeeded puts
			FutureRemove futureRemove = dataManager.removeVersionUnblocked(parameters);
			futureRemove.addListener(new BaseFutureAdapter<FutureRemove>() {
				@Override
				public void operationComplete(FutureRemove future) {
					if (future.isFailed()) {
						logger.warn("Put retry: Could not delete the newly put content. '{}'", parameters.toString());
					}

					dataManager.putUnblocked(parameters).addListener(FuturePutListener.this);
				}
			});
		} else {
			logger.error("Put verification failed. Could not put data after {} tries. '{}'", putTries, parameters.toString());
			notifyFailure();
		}
	}

	/**
	 * Loads digest and triggers a check.
	 */
	private void verifyPut() {
		// get data to verify if everything went correct
		FutureDigest digestFuture = dataManager.getDigestUnblocked(parameters);
		digestFuture.addListener(new BaseFutureAdapter<FutureDigest>() {
			@Override
			public void operationComplete(FutureDigest future) throws Exception {
				if (future.isFailed() || future.getRawDigest() == null || future.getRawDigest().isEmpty()) {
					logger.error("Put verification failed. Could not get digest. '{}'", parameters.toString());
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
			if (rawDigest.get(peerAddress) == null || rawDigest.get(peerAddress).keyDigest() == null
					|| rawDigest.get(peerAddress).keyDigest().isEmpty()) {
				logger.warn("Put verification: Received no digest from peer '{}'. '{}'", peerAddress, parameters.toString());
			} else {
				NavigableMap<Number640, Number160> keyDigest = rawDigest.get(peerAddress).keyDigest();

				if (keyDigest.firstEntry().getKey().getVersionKey().equals(parameters.getVersionKey())) {
					logger.trace("Put verification: On peer '{}' entry is newest. '{}'", peerAddress, parameters.toString());

				} else if (keyDigest.containsKey(parameters.getKey())) {
					logger.trace("Put verification: On peer '{}' entry exists in history. '{}'", peerAddress,
							parameters.toString());

				} else {
					logger.warn("Put verification: Concurrent modification on peer '{}' happened. '{}'", peerAddress,
							parameters.toString());

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
	protected boolean checkIfMyVerisonWins(NavigableMap<Number640, Number160> keyDigest, PeerAddress peerAddress) {
		/* Check if based on entry exists */
		if (!keyDigest.containsKey(new Number640(parameters.getLKey(), parameters.getDKey(), parameters.getCKey(),
				parameters.getData().getBasedOnKey()))) {
			logger.warn("Put verification: Peer '{}' does not contain based on version. '{}'", peerAddress,
					parameters.toString());
			// something is definitely wrong with this peer
			return true;
		} else {
			// figure out the next version based on same version
			Number640 entryBasingOnSameParent = getSuccessor(keyDigest);
			if (entryBasingOnSameParent == null) {
				if (keyDigest.firstKey().getVersionKey().equals(parameters.getData().getBasedOnKey())) {
					logger.error("Put verification: Peer '{}' has no successor version. '{}'", peerAddress,
							parameters.toString());
					// this peer doesn't contain any successor version, with this peer is something wrong
					return true;
				} else {
					logger.error("Put verification: Peer '{}' has a corrupt version history. '{}'", peerAddress,
							parameters.toString());
					return true;
				}
			} else {
				int compare = entryBasingOnSameParent.getVersionKey().compareTo(parameters.getVersionKey());
				if (compare == 0) {
					logger.error("Put verification: Peer '{}' has same version. '{}'", peerAddress, parameters.toString());
					return true;
				} else if (compare < 0) {
					logger.warn("Put verification: Peer '{}' has older version. '{}'", peerAddress, parameters.toString());
					return false;
				} else {
					logger.warn("Put verification: Peer '{}' has newer version. '{}'", peerAddress, parameters.toString());
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
			if (keyDigest.get(key).equals(parameters.getData().getBasedOnKey())) {
				entryBasingOnSameParent = key;
				break;
			}
		}
		return entryBasingOnSameParent;
	}

	private void notifySuccess() {
		logger.trace("Verification for put completed. '{}'", parameters.toString());
		// everything is ok
		success = true;
		latch.countDown();
	}

	/**
	 * Remove first potentially successful puts. Then notify the listener about the fail.
	 */
	private void notifyFailure() {
		// remove succeeded puts
		FutureRemove futureRemove = dataManager.removeVersionUnblocked(parameters);
		futureRemove.addListener(new BaseFutureAdapter<FutureRemove>() {
			@Override
			public void operationComplete(FutureRemove future) {
				if (future.isFailed()) {
					logger.warn("Put retry: Could not delete the newly put content. '{}'", parameters.toString());
				}

				success = false;
				latch.countDown();
			}
		});
	}
}
