package org.hive2hive.core.network.data.futures;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.FutureRemove;
import net.tomp2p.dht.StorageLayer.PutStatus;
import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.peers.Number640;
import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.BaseNetworkContent;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.DataManager.H2HPutStatus;
import org.hive2hive.core.network.data.parameters.IParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A put future adapter for verifying a put of a {@link BaseNetworkContent} object. Provides failure handling
 * and a blocking wait.</br></br>
 * 
 * <b>Failure Handling</b></br>
 * Putting can fail when the future object failed, when the future object contains wrong data or the
 * responding node detected a failure. See {@link PutStatus} for possible failures. If putting fails the
 * adapter retries it to a certain threshold (see {@link H2HConstants#PUT_RETRIES}). All puts are
 * asynchronous. That's why the future listener attaches himself to the new future objects so that the adapter
 * can finally notify his/her listener about a success or failure.
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
	// used as return value
	private H2HPutStatus status;

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
	public H2HPutStatus await() {
		try {
			latch.await(H2HConstants.AWAIT_NETWORK_OPERATION_MS * H2HConstants.PUT_RETRIES, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			logger.error("Could not wait until put has finished.", e);
		}

		return status;
	}

	@Override
	public void operationComplete(FuturePut future) throws Exception {
		if (future.isFailed()) {
			logger.warn("Put future was not successful. '{}'. Reason: {}", parameters.toString(), future.failedReason());
			retryPut();
			return;
		} else if (future.rawResult().isEmpty()) {
			logger.warn("Returned raw results are empty. '{}'", parameters.toString());
			retryPut();
			return;
		}

		// analyze returned put status
		List<PeerAddress> fail = new ArrayList<PeerAddress>();
		List<PeerAddress> versionFork = new ArrayList<PeerAddress>();
		for (PeerAddress peeradress : future.rawResult().keySet()) {
			Map<Number640, Byte> map = future.rawResult().get(peeradress);
			if (map == null) {
				logger.warn("A node gave no status (null) back. '{}'", parameters.toString());
				fail.add(peeradress);
			} else {
				for (Number640 key : map.keySet()) {
					byte status = map.get(key);
					if (status == -1) {
						logger.warn("Got an invalid status: {}", status);
						fail.add(peeradress);
					} else {
						switch (PutStatus.values()[status]) {
							case OK:
								break;
							case FAILED:
							case FAILED_SECURITY:
								logger.warn("A node denied putting data. reason = '{}'. '{}'", PutStatus.values()[status],
										parameters.toString());
								fail.add(peeradress);
								break;
							case VERSION_FORK:
								logger.warn("A node responded with a version fork. '{}'", parameters.toString());
								versionFork.add(peeradress);
								break;
							default:
								logger.warn("Got an unknown status: {}", PutStatus.values()[status]);
						}
					}
				}
			}
		}

		// check if majority of the contacted nodes responded with ok
		if ((double) fail.size() < ((double) future.rawResult().size()) / 2.0) {
			if (versionFork.isEmpty()) {
				if (parameters.hasPrepareFlag()) {
					// confirm put
					dataManager.confirmUnblocked(parameters).addListener(new BaseFutureAdapter<FuturePut>() {

						// used to count confirm retries
						private int confirmTries = 0;

						@Override
						public void operationComplete(FuturePut future) throws Exception {
							if (future.isFailed()) {
								logger.warn("Confirm future was not successful. Reason = '{}' {}", future.failedReason(),
										parameters.toString());
								retryConfirm();
								return;
							} else if (future.rawResult().isEmpty()) {
								logger.warn("Returned raw results are empty. {}", parameters.toString());
								retryConfirm();
								return;
							}

							// analyze returned status
							List<PeerAddress> fail = new ArrayList<PeerAddress>();
							for (PeerAddress peeradress : future.rawResult().keySet()) {
								Map<Number640, Byte> map = future.rawResult().get(peeradress);
								if (map == null) {
									logger.warn("A node gave no status (null) back. {}", parameters.toString());
									fail.add(peeradress);
								} else {
									for (Number640 key : future.rawResult().get(peeradress).keySet()) {
										byte status = future.rawResult().get(peeradress).get(key);
										switch (PutStatus.values()[status]) {
											case OK:
												break;
											case FAILED:
											case FAILED_SECURITY:
											case NOT_FOUND:
												logger.warn("A node could not confirm data. Reason = '{}'. {}",
														PutStatus.values()[status], parameters.toString());
												fail.add(peeradress);
												break;
											default:
												logger.warn("Got an unknown status = '{}' {}", PutStatus.values()[status],
														parameters.toString());
										}
									}
								}
							}

							// check if majority of the contacted nodes responded with ok
							if ((double) fail.size() < ((double) future.rawResult().size()) / 2.0) {
								status = H2HPutStatus.OK;
								latch.countDown();
							} else {
								logger.warn("{} of {} contacted nodes failed. {}", fail.size(), future.rawResult().size(),
										parameters.toString());
								retryConfirm();
							}
						}

						/**
						 * Retries a confirm till a certain threshold is reached (see
						 * {@link H2HConstants.CONFIRM_RETRIES}). A {@link RetryPutListener} tries to confirm
						 * again.
						 */
						private void retryConfirm() {
							if (confirmTries++ < H2HConstants.CONFIRM_RETRIES) {
								logger.warn("Confirm retry #{}. {}", confirmTries, parameters.toString());
								// retry confirmation, attach itself as listener
								dataManager.confirmUnblocked(parameters).addListener(this);
							} else {
								logger.error("Could not confirm put after {} tries. {}", confirmTries, parameters.toString());
								status = H2HPutStatus.FAILED;
								latch.countDown();
							}
						}
					});
				} else {
					status = H2HPutStatus.OK;
					latch.countDown();
				}
			} else {
				logger.warn("Version fork after put detected. Rejecting put.");
				// reject put
				dataManager.removeVersionUnblocked(parameters).addListener(new BaseFutureAdapter<FutureRemove>() {
					@Override
					public void operationComplete(FutureRemove future) {
						if (future.isFailed()) {
							logger.warn("Could not delete the prepared put. '{}'", parameters.toString());
						}
						status = H2HPutStatus.VERSION_FORK;
						latch.countDown();
					}
				});
			}
		} else {
			logger.warn("{} of {} contacted nodes failed.", fail.size(), future.rawResult().size());
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
			// remove prior put
			dataManager.removeVersionUnblocked(parameters).addListener(new BaseFutureAdapter<FutureRemove>() {
				@Override
				public void operationComplete(FutureRemove future) {
					if (future.isFailed()) {
						logger.warn("Could not delete the newly put content. '{}'", parameters.toString());
					}
					// retry put, attach itself as listener
					dataManager.putUnblocked(parameters).addListener(FuturePutListener.this);
				}
			});
		} else {
			logger.error("Could not put data after {} tries. '{}'", putTries, parameters.toString());
			// remove prior put
			dataManager.removeVersionUnblocked(parameters).addListener(new BaseFutureAdapter<FutureRemove>() {
				@Override
				public void operationComplete(FutureRemove future) {
					if (future.isFailed()) {
						logger.warn("Could not delete the newly put content. '{}'", parameters.toString());
					}
					status = H2HPutStatus.FAILED;
					latch.countDown();
				}
			});
		}
	}

}
