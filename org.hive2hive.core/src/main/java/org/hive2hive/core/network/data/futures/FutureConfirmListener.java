package org.hive2hive.core.network.data.futures;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.StorageLayer.PutStatus;
import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.peers.Number640;
import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.IDataManager.H2HPutStatus;
import org.hive2hive.core.network.data.parameters.IParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Seppi
 */
public class FutureConfirmListener extends BaseFutureAdapter<FuturePut> {

	private static final Logger logger = LoggerFactory.getLogger(FutureConfirmListener.class);

	private final IParameters parameters;
	private final DataManager dataManager;
	private final CountDownLatch latch;

	private H2HPutStatus status;

	// used to count confirm retries
	private int confirmTries = 0;

	public FutureConfirmListener(IParameters parameters, DataManager dataManager) {
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
			latch.await();
		} catch (InterruptedException e) {
			logger.error("Could not wait until confirm has finished.", e);
		}

		return status;
	}

	@Override
	public void operationComplete(FuturePut future) throws Exception {
		if (future.isFailed()) {
			logger.warn("Confirm future was not successful. '{}'", parameters.toString());
			retryConfirm();
			return;
		} else if (future.rawResult().isEmpty()) {
			logger.warn("Returned raw results are empty. '{}'", parameters.toString());
			retryConfirm();
			return;
		}

		// analyze returned status
		List<PeerAddress> fail = new ArrayList<PeerAddress>();
		for (PeerAddress peeradress : future.rawResult().keySet()) {
			Map<Number640, Byte> map = future.rawResult().get(peeradress);
			if (map == null) {
				logger.warn("A node gave no status (null) back. '{}'", parameters.toString());
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
							logger.warn("A node could not confirm data. reason = '{}'. '{}'", PutStatus.values()[status],
									parameters.toString());
							fail.add(peeradress);
							break;
						default:
							logger.warn("Got an unknown status: {}", PutStatus.values()[status]);
					}
				}
			}
		}

		// check if majority of the contacted nodes responded with ok
		if ((double) fail.size() < ((double) future.rawResult().size()) / 2.0) {
			status = H2HPutStatus.OK;
			latch.countDown();
		} else {
			logger.warn("{} of {} contacted nodes failed.", fail.size(), future.rawResult().size());
			retryConfirm();
		}
	}

	/**
	 * Retries a confirm till a certain threshold is reached (see {@link H2HConstants.CONFIRM_RETRIES}). A
	 * {@link RetryPutListener} tries to confirm again.
	 */
	private void retryConfirm() {
		if (confirmTries++ < H2HConstants.CONFIRM_RETRIES) {
			logger.warn("Confirm retry #{}. '{}'", confirmTries, parameters.toString());
			// retry confirmation, attach itself as listener
			dataManager.confirmUnblocked(parameters).addListener(FutureConfirmListener.this);
		} else {
			logger.error("Could not confirm put after {} tries. '{}'", confirmTries, parameters.toString());
			status = H2HPutStatus.FAILED;
			latch.countDown();
		}
	}

}
