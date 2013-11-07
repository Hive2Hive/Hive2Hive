package org.hive2hive.core.process.common.get;

import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.FutureGet;
import net.tomp2p.futures.FutureRemove;

import org.apache.log4j.Logger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.process.ProcessStep;

/**
 * A process step which gets a {@link NetworkContent} object under the given keys. </br>
 * <b>Important:</b> Use only this process step to get some data into the network so that in case of failure a
 * appropriate handling is triggered.
 * 
 * @author Nico
 */
public abstract class BaseGetProcessStep extends ProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(BaseGetProcessStep.class);

	protected final String locationKey;
	protected final String contentKey;

	public BaseGetProcessStep(String locationKey, String contentKey) {
		this.locationKey = locationKey;
		this.contentKey = contentKey;
	}

	@Override
	public void start() {
		get(locationKey, contentKey);
	}

	protected void get(final String locationKey, final String contentKey) {
		FutureGet getFuture = getNetworkManager().getGlobal(locationKey, contentKey);
		getFuture.addListener(new GetListener());
	}

	@Override
	public void rollBack() {
		// ignore because just a get
	}

	protected abstract void handleGetResult(NetworkContent content);

	@Override
	protected void handleRemovalResult(FutureRemove future) {
		// ignore
	}

	/**
	 * Verifies a get.
	 * 
	 * @author Nico
	 * 
	 */
	private class GetListener extends BaseFutureAdapter<FutureGet> {
		@Override
		public void operationComplete(FutureGet future) throws Exception {
			logger.debug("Start verification of get(" + locationKey + ", " + contentKey + ")");

			// TODO take newest version from possibly multiple results
			if (future.getData() == null) {
				handleGetResult(null);
			} else {
				handleGetResult((NetworkContent) future.getData().object());
			}
		}
	}
}
