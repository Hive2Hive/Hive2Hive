package org.hive2hive.core.network.data.futures;

import java.util.NavigableMap;
import java.util.concurrent.CountDownLatch;

import net.tomp2p.futures.BaseFutureListener;
import net.tomp2p.futures.FutureDigest;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;

import org.hive2hive.core.network.data.parameters.IParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A future listener for a get digest. It can be blocked until the result is here. Then, it returns the
 * desired content or <code>null</code> if the get fails or the content doesn't exist.
 * 
 * @author Seppi
 */
public class FutureDigestListener implements BaseFutureListener<FutureDigest> {

	private static final Logger logger = LoggerFactory.getLogger(FutureDigestListener.class);

	private final IParameters parameters;
	private final CountDownLatch latch;

	// the result when it came back
	private NavigableMap<Number640, Number160> result = null;

	public FutureDigestListener(IParameters parameters) {
		this.parameters = parameters;
		this.latch = new CountDownLatch(1);
	}

	/**
	 * Waits (blocking) until the operation is done
	 * 
	 * @return returns the content from the DHT
	 */
	public NavigableMap<Number640, Number160> awaitAndGet() {
		try {
			latch.await();
		} catch (InterruptedException e) {
			logger.error("Latch to wait for the get was interrupted.");
			return null;
		}
		return result;
	}

	@Override
	public void operationComplete(FutureDigest future) throws Exception {
		if (future.isFailed()) {
			logger.error("Could not get digest. '{}'", parameters.toString());
		} else {
			result = future.getDigest().keyDigest();
			if (result == null) {
				logger.warn("Got digest null. '{}'", parameters.toString());
			} else {
				logger.debug("Got digest. '{}'", parameters.toString());
			}
		}
		latch.countDown();
	}

	@Override
	public void exceptionCaught(Throwable t) throws Exception {
		logger.error(String.format("Exception caught during get digest. %s reason = '{}'", parameters.toString()),
				t.getMessage());
		operationComplete(null);
	}

}
