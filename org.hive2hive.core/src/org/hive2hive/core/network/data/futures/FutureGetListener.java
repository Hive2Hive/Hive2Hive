package org.hive2hive.core.network.data.futures;

import java.util.concurrent.CountDownLatch;

import net.tomp2p.futures.BaseFutureListener;
import net.tomp2p.futures.FutureGet;
import net.tomp2p.peers.Number160;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.network.data.parameters.IParameters;

/**
 * A future listener for a get. It can be blocked until the result is here. Then, it returns the desired
 * content or <code>null</code> if the get fails or the content doesn't exist.
 * 
 * @author Seppi, Nico
 */
public class FutureGetListener implements BaseFutureListener<FutureGet> {

	private final static Logger logger = H2HLoggerFactory.getLogger(FutureGetListener.class);

	private final IParameters parameters;
	private final DataManager dataManager;
	private final CountDownLatch latch;

	// the result when it came back
	private NetworkContent result = null;

	// flag for retries
	private boolean retry = true;
	// used to count get retries
	private int getTries = 0;

	public FutureGetListener(IParameters parameters, boolean retry, DataManager dataManager) {
		this.retry = retry;
		this.parameters = parameters;
		this.dataManager = dataManager;
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
			logger.error("Latch to wait for the get was interrupted");
			return null;
		}

		return result;
	}

	@Override
	public void operationComplete(FutureGet future) throws Exception {
		if (retry) {
			if (future == null || future.isFailed() || future.getData() == null) {
				if (getTries++ < H2HConstants.GET_RETRIES) {
					logger.warn(String.format("Get retry #%s because future failed. %s", getTries,
							parameters.toString()));
					if (parameters.getVersionKey().equals(Number160.ZERO))
						dataManager.getUnblocked(parameters).addListener(this);
					else
						dataManager.getVersionUnblocked(parameters).addListener(this);
				} else {
					logger.warn(String.format("Get failed after %s tries. %s", getTries,
							parameters.toString()));
					notify(null);
				}
			} else {
				NetworkContent content = (NetworkContent) future.getData().object();
				if (content == null) {
					logger.warn(String.format("Get retry #%s because content is null. %s", getTries,
							parameters.toString()));
					if (parameters.getVersionKey().equals(Number160.ZERO))
						dataManager.getUnblocked(parameters).addListener(this);
					else
						dataManager.getVersionUnblocked(parameters).addListener(this);
				} else {
					notify(content);
				}
			}
		} else {
			if (future.getData() == null) {
				notify(null);
			} else {
				NetworkContent content = (NetworkContent) future.getData().object();

				notify(content);
			}
		}
	}

	/**
	 * Sets the result and releases the lock
	 * 
	 * @param result
	 */
	private void notify(NetworkContent result) {
		if (result == null) {
			logger.warn(String.format("Got null. %s", parameters.toString()));
		} else {
			logger.debug(String.format("got result = '%s' %s", result.getClass().getSimpleName(),
					parameters.toString()));
		}

		this.result = result;
		latch.countDown();
	}

	@Override
	public void exceptionCaught(Throwable t) throws Exception {
		logger.error("Exception caught during get for " + parameters.toString(), t);
		operationComplete(null);
	}

}
