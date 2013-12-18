package org.hive2hive.core.network.data.futures;

import net.tomp2p.futures.BaseFutureListener;
import net.tomp2p.futures.FutureGet;

import org.apache.log4j.Logger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.network.data.listener.IGetListener;

/**
 * A future listener for a get. Returns the given {@link IGetListener} listener the desired content or
 * <code>null</code> if the get fails or the content doesn't exist.
 * 
 * @author Seppi
 */
public class FutureGetListener implements BaseFutureListener<FutureGet> {

	private final static Logger logger = H2HLoggerFactory.getLogger(FutureGetListener.class);
	private final IGetListener listener;
	private final String locationKey;

	public FutureGetListener(IGetListener listener, String locationKey) {
		this.listener = listener;
		this.locationKey = locationKey;
	}

	@Override
	public void operationComplete(FutureGet future) throws Exception {
		if (future == null || future.isFailed() || future.getData() == null) {
			logger.debug(String.format("got key = '%s' result= 'null'", locationKey));
			if (listener != null)
				listener.handleGetResult(null);
		} else {
			NetworkContent content = (NetworkContent) future.getData().object();
			logger.debug(String.format("got key = '%s' result= '%s'", locationKey, content.getClass()
					.getSimpleName()));
			if (listener != null)
				listener.handleGetResult(content);
		}
	}

	@Override
	public void exceptionCaught(Throwable t) throws Exception {
		logger.error("Exception caught during get for key '" + locationKey + "'", t);
		operationComplete(null);
	}

}
