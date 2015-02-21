package org.hive2hive.core.network.data.futures;

import io.netty.buffer.ByteBuf;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.futures.BaseFutureListener;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.BaseNetworkContent;
import org.hive2hive.core.network.data.parameters.IParameters;
import org.hive2hive.core.serializer.IH2HSerialize;
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
	private final IH2HSerialize serializer;
	private final CountDownLatch latch;

	// the result when it came back
	private BaseNetworkContent result = null;

	public FutureGetListener(IParameters parameters, IH2HSerialize serializer) {
		this.parameters = parameters;
		this.serializer = serializer;
		this.latch = new CountDownLatch(1);
	}

	/**
	 * Waits (blocking) until the operation is done
	 * 
	 * @return returns the content from the DHT
	 */
	public BaseNetworkContent awaitAndGet() {
		try {
			latch.await(H2HConstants.AWAIT_NETWORK_OPERATION_MS, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			logger.error("Latch to wait for the get was interrupted.");
		}
		return result;
	}

	@Override
	public void operationComplete(FutureGet future) throws Exception {
		if (future == null || future.isFailed() || future.isEmpty() || future.data() == null) {
			result = null;
			logger.debug("Got null. '{}'", parameters.toString());
		} else {
			// set the result
			ByteBuf byteBuf = future.data().buffer();
			if (byteBuf.isReadable()) {
				byte[] buffer = new byte[byteBuf.readableBytes()];
				byteBuf.readBytes(buffer);
				result = (BaseNetworkContent) serializer.deserialize(buffer);
				logger.debug("Got result = '{}'. '{}'", result.getClass().getSimpleName(), parameters.toString());
			} else {
				result = null;
				logger.debug("Got null. '{}'", parameters.toString());
			}
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
