package org.hive2hive.core.network.data.futures;

import java.util.concurrent.CountDownLatch;

import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.FutureDigest;
import net.tomp2p.futures.FutureRemove;
import net.tomp2p.p2p.builder.DigestBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.parameters.IParameters;

/**
 * A future listener for a remove. After the operation completed the listener verifies with a get digest if
 * all data has been deleted. If not, the listener retries the remove (see {@link H2HConstants#REMOVE_RETRIES}
 * ).
 * 
 * @author Seppi, Nico
 */
public class FutureRemoveListener extends BaseFutureAdapter<FutureRemove> {

	private final static Logger logger = H2HLoggerFactory.getLogger(FutureRemoveListener.class);

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
			logger.error("Could not wait until put has finished", e);
		}

		return success;
	}

	@Override
	public void operationComplete(FutureRemove future) throws Exception {
		logger.debug(String.format("Start verification of remove. %s", parameters.toString()));
		// get data to verify if everything went correct
		DigestBuilder digestBuilder = dataManager.getDigest(parameters.getLKey());
		if (versionRemove) {
			digestBuilder.setDomainKey(parameters.getDKey()).setContentKey(parameters.getCKey())
					.setVersionKey(parameters.getVersionKey());
		} else {
			digestBuilder.from(
					new Number640(parameters.getLKey(), parameters.getDKey(), parameters.getCKey(),
							Number160.ZERO)).to(
					new Number640(parameters.getLKey(), parameters.getDKey(), parameters.getCKey(),
							Number160.MAX_VALUE));
		}
		FutureDigest digestFuture = digestBuilder.start();
		digestFuture.addListener(new BaseFutureAdapter<FutureDigest>() {
			@Override
			public void operationComplete(FutureDigest future) throws Exception {
				if (future.getDigest() == null) {
					retryRemove();
				} else if (!future.getDigest().keyDigest().isEmpty()) {
					retryRemove();
				} else {
					logger.debug(String.format("Verification for remove completed. %s", parameters.toString()));
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
			logger.warn(String.format("Remove verification failed. Data is not null. Try #%s. %s",
					removeTries, parameters.toString()));
			if (!versionRemove) {
				dataManager.removeUnblocked(parameters).addListener(this);
			} else {
				dataManager.removeVersionUnblocked(parameters).addListener(this);
			}
		} else {
			logger.error(String.format("Remove verification failed. Data is not null after %s tries. %s",
					removeTries - 1, parameters.toString()));
			success = false;
			latch.countDown();
		}
	}
}
