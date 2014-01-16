package org.hive2hive.core.network.data.futures;

import net.tomp2p.futures.BaseFutureListener;
import net.tomp2p.futures.FutureGet;
import net.tomp2p.peers.Number160;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.data.DataManager;
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
	private final Number160 locationKey;
	private final Number160 domainKey;
	private final Number160 contentKey;
	private final Number160 versionKey;
	private final DataManager dataManager;

	// flag for retries
	private boolean retry = true;
	// used to count get retries
	private int getTries = 0;

	public FutureGetListener(Number160 locationKey, Number160 domainKey, Number160 contentKey,
			DataManager dataManager, IGetListener listener) {
		this(locationKey, domainKey, contentKey, Number160.ZERO, dataManager, listener);
	}

	public FutureGetListener(Number160 locationKey, Number160 domainKey, DataManager dataManager,
			IGetListener listener) {
		this(locationKey, domainKey, null, null, dataManager, listener);
		this.retry = false;
	}

	public FutureGetListener(Number160 locationKey, Number160 domainKey, Number160 contentKey,
			Number160 versionKey, DataManager dataManager, IGetListener listener) {
		this.locationKey = locationKey;
		this.domainKey = domainKey;
		this.contentKey = contentKey;
		this.versionKey = versionKey;
		this.listener = listener;
		this.dataManager = dataManager;
	}

	@Override
	public void operationComplete(FutureGet future) throws Exception {
		if (retry) {
			if (future == null || future.isFailed() || future.getData() == null) {
				if (getTries++ < H2HConstants.GET_RETRIES) {
					logger.warn(String
							.format("Get retry #%s because future failed. location key = '%s' domain key = '%s' content key = '%s' version key = '%s'",
									getTries, locationKey, domainKey, contentKey, versionKey));
					if (versionKey == Number160.ZERO)
						dataManager.get(locationKey, domainKey, contentKey).addListener(this);
					else
						dataManager.get(locationKey, domainKey, contentKey, versionKey).addListener(this);
				} else {
					logger.warn(String
							.format("Get failed after %s tries. location key = '%s' domain key = '%s' content key = '%s' version key '%s'",
									getTries, locationKey, domainKey, contentKey, versionKey));
					if (listener != null)
						listener.handleGetResult(null);
				}
			} else {
				NetworkContent content = (NetworkContent) future.getData().object();
				if (content == null) {
					logger.warn(String
							.format("Get retry #%s because content is null. location key = '%s' domain key = '%s' content key = '%s' version key = '%s'",
									getTries, locationKey, domainKey, contentKey, versionKey));
					if (versionKey == Number160.ZERO)
						dataManager.get(locationKey, domainKey, contentKey).addListener(this);
					else
						dataManager.get(locationKey, domainKey, contentKey, versionKey).addListener(this);
				} else {
					logger.debug(String
							.format("got result = '%s' location key = '%s' domain key = '%s' content key = '%s' version key '%s'",
									content.getClass().getSimpleName(), locationKey, domainKey, contentKey,
									versionKey));
					if (listener != null)
						listener.handleGetResult(content);
				}
			}
		} else {
			if (future.getData() == null) {
				logger.warn(String
						.format("Got null. location key = '%s' domain key = '%s' content key = '%s' version key = '%s'",
								locationKey, domainKey, contentKey, versionKey));
				if (listener != null)
					listener.handleGetResult(null);
			} else {
				NetworkContent content = (NetworkContent) future.getData().object();
				logger.debug(String
						.format("got result = '%s' location key = '%s' domain key = '%s' content key = '%s' version key '%s'",
								content.getClass().getSimpleName(), locationKey, domainKey, contentKey,
								versionKey));
				if (listener != null)
					listener.handleGetResult(content);
			}
		}
	}

	@Override
	public void exceptionCaught(Throwable t) throws Exception {
		logger.error("Exception caught during get for key '" + locationKey + "'", t);
		operationComplete(null);
	}

}
