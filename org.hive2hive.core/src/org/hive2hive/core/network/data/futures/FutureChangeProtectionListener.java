package org.hive2hive.core.network.data.futures;

import java.util.concurrent.CountDownLatch;

import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.FuturePut;
import net.tomp2p.peers.Number160;

import org.apache.log4j.Logger;
import org.hive2hive.core.log.H2HLoggerFactory;

/**
 * Simple blocking listener to change the protection key. In contrast to the {@link FuturePutListener} this
 * listener does not re-try at failure but instantly return a fail.
 * 
 * @author Nico, Seppi
 */
public class FutureChangeProtectionListener extends BaseFutureAdapter<FuturePut> {

	private final static Logger logger = H2HLoggerFactory.getLogger(FutureChangeProtectionListener.class);

	private final Number160 locationKey;
	private final Number160 domainKey;
	private final Number160 contentKey;
	private final Number160 versionKey;
	private final CountDownLatch latch;

	private boolean success = false;

	public FutureChangeProtectionListener(Number160 locationKey, Number160 domainKey, Number160 contentKey,
			Number160 versionKey) {
		this.locationKey = locationKey;
		this.domainKey = domainKey;
		this.contentKey = contentKey;
		this.versionKey = versionKey;
		this.latch = new CountDownLatch(1);
	}

	/**
	 * Wait (blocking) until the change of the protection key is done
	 * 
	 * @return true if successful, false if not successful
	 */
	public boolean await() {
		try {
			latch.await();
		} catch (InterruptedException e) {
			logger.error("Could not wait until the protection key change has finished", e);
		}

		return success;
	}

	@Override
	public void operationComplete(FuturePut future) throws Exception {
		if (future.isFailed()) {
			logger.warn(String
					.format("Change was not successful. location key = '%s' domain key = '%s' content key = '%s' version key = '%s'",
							locationKey, domainKey, contentKey, versionKey));
			success = false;
			latch.countDown();
		} else {
			logger.trace(String
					.format("Change of protection key successful. location key = '%s' domain key = '%s' content key = '%s'  version key = '%s'",
							locationKey, domainKey, contentKey, versionKey));
			success = true;
			latch.countDown();
		}
	}

}
