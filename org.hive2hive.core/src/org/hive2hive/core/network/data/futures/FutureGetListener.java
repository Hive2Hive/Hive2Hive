package org.hive2hive.core.network.data.futures;

import java.util.concurrent.CountDownLatch;

import net.tomp2p.futures.BaseFutureListener;
import net.tomp2p.futures.FutureGet;
import net.tomp2p.peers.Number160;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.NetworkContent;
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

	private final static Logger logger = LoggerFactory.getLogger(FutureGetListener.class);

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
			logger.error("Latch to wait for the get was interrupted.");
			return null;
		}

		return result;
	}

	@Override
	public void operationComplete(FutureGet future) throws Exception {
		if (retry) {
			if (future == null || future.isFailed() || future.getData() == null) {
				if (getTries++ < H2HConstants.GET_RETRIES) {
					logger.debug("Get retry #{} because future failed. '{}'", getTries, parameters.toString());
					if (parameters.getVersionKey().equals(Number160.ZERO))
						dataManager.getUnblocked(parameters).addListener(this);
					else
						dataManager.getVersionUnblocked(parameters).addListener(this);
				} else {
					logger.debug("Get failed after {} tries. '{}'", getTries, parameters.toString());
					notify(null);
				}
			} else {
				NetworkContent content = (NetworkContent) future.getData().object();
				if (content == null) {
					logger.debug("Get retry #{} because content is null. '{}'", getTries,
							parameters.toString());
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
			logger.warn("Got null. '{}'", parameters.toString());
		} else {
			logger.debug("Got result = '{}'. '{}'", result.getClass().getSimpleName(), parameters.toString());
		}

		this.result = result;
		latch.countDown();
	}

	@Override
	public void exceptionCaught(Throwable t) throws Exception {
		logger.error(String.format("Exception caught during get. %s reason = '{}'", parameters.toString()),
				t.getMessage());
		operationComplete(null);
	}

}
