package org.hive2hive.core.process.common.put;

import java.util.HashMap;
import java.util.Map;

import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.FutureGet;
import net.tomp2p.futures.FuturePut;
import net.tomp2p.futures.FutureRemove;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.StorageGeneric.PutStatus;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.process.ProcessStep;

/**
 * A process step which puts a {@link NetworkContent} object under the given keys. </br>
 * <b>Important:</b> Use only this process step to put some data into the network so that in case of failure an
 * appropriate handling is triggered.
 * 
 * @author Seppi
 */
public class PutProcessStep extends ProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(PutProcessStep.class);

	protected String locationKey;
	protected String contentKey;
	protected NetworkContent content;
	protected ProcessStep nextStep;

	// used to count put retries
	private int putTries = 0;
	// used to count get tries
	private int getTries = 0;

	public PutProcessStep(String locationKey, String contentKey, NetworkContent content, ProcessStep nextStep) {
		this.locationKey = locationKey;
		this.contentKey = contentKey;
		this.content = content;
		this.nextStep = nextStep;
	}

	@Override
	public void start() {
		put(locationKey, contentKey, content);
	}

	protected final void put(String locationKey, String contentKey, NetworkContent content) {
		// can be called from subclasses, make sure to store the correct attributes here
		this.locationKey = locationKey;
		this.contentKey = contentKey;
		this.content = content;

		FuturePut putFuture = getNetworkManager().putGlobal(this.locationKey, this.contentKey, this.content);
		putFuture.addListener(new PutVerificationListener());
	}

	@Override
	public void rollBack() {
		// TODO extend the remove method with the version key
		// remove(locationKey, contentKey);
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
			logger.debug(String.format(
					"Verification for put completed. location key = '%s' content key = '%s'", locationKey,
					contentKey));
			// everything is ok, continue with next step
			getProcess().setNextStep(nextStep);
		} else {
			logger.warn(String
					.format("Put verification failed. Concurrent modification happened. location key = '%s' content key = '%s'",
							locationKey, contentKey));
			getProcess().stop("Put verification failed. Reason: Concurrent modification happened.");
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
			FutureGet getFuture = getNetworkManager().getGlobal(locationKey, contentKey);
			getFuture.addListener(new BaseFutureAdapter<FutureGet>() {
				@Override
				public void operationComplete(FutureGet future) throws Exception {
					if (future.isFailed() || future.getData() == null) {
						logger.warn(String
								.format("Put verification failed. Couldn't get data. Try #%s. location key = '%s' content key = '%s'",
										getTries, locationKey, contentKey));
						verifyWithADelayedGet();
					} else {
						checkVersionKey();
					}
				}
			});
		} else {
			logger.error(String
					.format("Put verification failed. Couldn't get data after %s tries. location key = '%s' content key = '%s'",
							getTries - 1, locationKey, contentKey));
			getProcess().stop("Put verification failed. Reason: Couldn't get data.");
		}
	}

	private void retryPut() {
		if (putTries++ < H2HConstants.PUT_RETRIES) {
			logger.warn(String
					.format("Put verification failed. Couldn't put data. Try #%s. location key = '%s' content key = '%s'",
							putTries, locationKey, contentKey));
			getNetworkManager().remove(locationKey, contentKey).addListener(
					new BaseFutureAdapter<FutureRemove>() {
						@Override
						public void operationComplete(FutureRemove future) throws Exception {
							if (future.isFailed()) {
								logger.warn("Put Retry: Could not delete the newly put content");
							}
							put(locationKey, contentKey, content);
						}
					});
		} else {
			logger.error(String
					.format("Put verification failed. Couldn't put data after %s tries. location key = '%s' content key = '%s'",
							putTries, locationKey, contentKey));
			getProcess().stop(
					String.format("Put verification failed. Couldn't put data after %s tries.", putTries));
		}
	}

	/**
	 * Verifies a put by checking if all replicas accepted the content. If not, the version is compared and
	 * the newest version is taken. Old deprecated versions are cleaned up.
	 * After the verification, the listeners on this object are called.
	 * 
	 * This class is needed because there could emerge inconsistent states when using replication
	 * 
	 * @author Nico, Seppi
	 * 
	 */
	private class PutVerificationListener extends BaseFutureAdapter<FuturePut> {

		@Override
		public void operationComplete(FuturePut future) throws Exception {
			logger.debug(String.format("Start verification of put. location key = '%s' content key = '%s'",
					locationKey, contentKey));

			if (future.isFailed() || future.getRawResult().isEmpty()) {
				retryPut();
			} else {
				// analyze returned PutStatus
				Map<PeerAddress, Byte> statusMap = new HashMap<>();
				for (PeerAddress peeradress : future.getRawResult().keySet()) {
					statusMap.put(peeradress, future.getRawResult().get(peeradress).values().iterator()
							.next());
				}
				int failedNodes = 0;
				int contactedNodes = future.getRawResult().keySet().size();
				for (Byte status : statusMap.values()) {
					if (PutStatus.values()[status] == PutStatus.VERSION_CONFLICT) {
						// TODO implement version conflict
						// checkVersionKey();
						logger.warn(String.format(
								"A version conflict detected. location key = '%s' content key = '%s'",
								locationKey, contentKey));
						return;
					} else if (PutStatus.values()[status] != PutStatus.OK) {
						logger.warn(String
								.format("A node denied putting data. reason = '%s' location key = '%s' content key = '%s'",
										PutStatus.values()[status], locationKey, contentKey));
						failedNodes++;
					}
				}

				logger.debug(String.format("contacted nodes = '%s' failed nodes = '%s'", contactedNodes,
						failedNodes));

				// when the majority of the contacted nodes responded with ok everything is ok
				if ((double) failedNodes < ((double) contactedNodes) / 2.0) {
					waitAMoment();
					verifyWithADelayedGet();
				} else {
					retryPut();
				}
			}
		}
	}

}
