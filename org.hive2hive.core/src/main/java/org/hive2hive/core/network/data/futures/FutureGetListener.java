package org.hive2hive.core.network.data.futures;

import java.util.concurrent.CountDownLatch;

import net.tomp2p.futures.BaseFutureListener;
import net.tomp2p.futures.FutureGet;

import org.hive2hive.core.model.NetworkContent;
import org.hive2hive.core.network.data.parameters.IParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A future listener for a get. It can be blocked until the result is here. Then, it returns the desired
 * content or <code>null</code> if the get fails or the content doesn't exist.
 * 
 * @author Seppi, Nico
 */
public class FutureGetListener implements BaseFutureListener<FutureGet> {

	private static final Logger logger = LoggerFactory.getLogger(FutureGetListener.class);

	private final IParameters parameters;
	private final CountDownLatch latch;

	// the result when it came back
	private NetworkContent result = null;

	public FutureGetListener(IParameters parameters) {
		this.parameters = parameters;
		this.latch = new CountDownLatch(1);
	}

	/**
	 * Waits (blocking) until the operation is done
	 * 
	 * @return returns the content from the DHT
	 */
	public NetworkContent awaitAndGet() {
		try {
			latch.await();
		} catch (InterruptedException e) {
			logger.error("Latch to wait for the get was interrupted.");
			return null;
		}
		return result;
	}

	@Override
	public void operationComplete(FutureGet future) throws Exception {
		if (future == null || future.isFailed() || future.getData() == null) {
			result = null;
			logger.debug("Got null. '{}'", parameters.toString());
		} else {
			// set the result
			result = (NetworkContent) future.getData().object();
			logger.debug("Got result = '{}'. '{}'", result.getClass().getSimpleName(), parameters.toString());
		}
		// release the lock
		latch.countDown();
	}

	@Override
	public void exceptionCaught(Throwable t) throws Exception {
		logger.error(String.format("Exception caught during get. %s reason = '{}'", parameters.toString()), t.getMessage());
		operationComplete(null);
	}

}
