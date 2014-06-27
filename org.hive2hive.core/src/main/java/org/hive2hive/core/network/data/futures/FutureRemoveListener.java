package org.hive2hive.core.network.data.futures;

import java.util.concurrent.CountDownLatch;

import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.FutureDigest;
import net.tomp2p.futures.FutureRemove;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.parameters.IParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A future listener for a remove. After the operation completed the listener verifies with a get digest if
 * all data has been deleted. If not, the listener retries the remove (see {@link H2HConstants#REMOVE_RETRIES}
 * ).
 * 
 * @author Seppi, Nico
 */
public class FutureRemoveListener extends BaseFutureAdapter<FutureRemove> {

	private static final Logger logger = LoggerFactory.getLogger(FutureRemoveListener.class);

	// used to count remove retries
	private int removeTries = 0;

	private final IParameters parameters;
	private final boolean versionRemove;
	private final DataManager dataManager;
	private final CountDownLatch latch;
	private boolean success = false;

	public FutureRemoveListener(IParameters parameters, boolean versionRemove, DataManager dataManager) {
		this.parameters = parameters;
		this.versionRemove = versionRemove;
		this.dataManager = dataManager;
		this.latch = new CountDownLatch(1);
	}

	/**
	 * Wait (blocking) until the remove is done
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
	public void operationComplete(FutureRemove future) throws Exception {
		logger.trace("Start verification of remove. '{}'", parameters.toString());
		// get data to verify if everything went correct
		FutureDigest digestFuture = dataManager.getDigestUnblocked(parameters);
		digestFuture.addListener(new BaseFutureAdapter<FutureDigest>() {
			@Override
			public void operationComplete(FutureDigest future) throws Exception {
				if (future.getDigest() == null) {
					retryRemove();
				} else if (!future.getDigest().keyDigest().isEmpty()) {
					retryRemove();
				} else {
					logger.trace("Verification for remove completed. '{}'", parameters.toString());
					success = true;
					latch.countDown();
				}
			}
		});
	}

	/**
	 * Retry a remove till a certain threshold (see {@link H2HConstants#REMOVE_RETRIES})
	 */
	private void retryRemove() {
		if (removeTries++ < H2HConstants.REMOVE_RETRIES) {
			logger.warn("Remove verification failed. Data is not null. Try #{}. '{}'", removeTries, parameters.toString());
			if (!versionRemove) {
				dataManager.removeUnblocked(parameters).addListener(this);
			} else {
				dataManager.removeVersionUnblocked(parameters).addListener(this);
			}
		} else {
			logger.error("Remove verification failed. Data is not null after {} tries. '{}'", removeTries - 1,
					parameters.toString());
			success = false;
			latch.countDown();
		}
	}
}
