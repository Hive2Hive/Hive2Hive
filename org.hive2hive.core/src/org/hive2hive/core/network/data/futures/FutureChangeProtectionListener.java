package org.hive2hive.core.network.data.futures;

import java.util.concurrent.CountDownLatch;

import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.FuturePut;

import org.apache.log4j.Logger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.data.parameters.IParameters;

/**
 * Simple blocking listener to change the protection key. In contrast to the {@link FuturePutListener} this
 * listener does not re-try at failure but instantly return a fail.
 * 
 * @author Nico, Seppi
 */
public class FutureChangeProtectionListener extends BaseFutureAdapter<FuturePut> {

	private final static Logger logger = H2HLoggerFactory.getLogger(FutureChangeProtectionListener.class);

	private final IParameters parameters;
	private final CountDownLatch latch;

	private boolean success = false;

	public FutureChangeProtectionListener(IParameters parameters) {
		this.parameters = parameters;
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
			logger.error(String.format(
					"Could not wait until the protection key change has finished. reson = '%s' %s",
					e.getMessage(), parameters.toString()));
		}

		return success;
	}

	@Override
	public void operationComplete(FuturePut future) throws Exception {
		if (future.isFailed()) {
			logger.warn(String.format("Change was not successful. %s", parameters.toString()));
			success = false;
			latch.countDown();
		} else {
			logger.trace(String.format("Change of protection key successful. %s", parameters.toString()));
			success = true;
			latch.countDown();
		}
	}

}
