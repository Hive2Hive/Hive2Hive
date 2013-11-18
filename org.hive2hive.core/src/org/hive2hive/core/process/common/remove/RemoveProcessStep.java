package org.hive2hive.core.process.common.remove;

import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.FutureGet;
import net.tomp2p.futures.FutureRemove;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.process.ProcessStep;

/**
 * A process step which removes a {@link NetworkContent} object under the given keys from the network.</br>
 * <b>Important:</b> Use only this process step to remove data from the network so that in case of failure a
 * appropriate handling is triggered.
 * 
 * @author Seppi
 */
public class RemoveProcessStep extends ProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(RemoveProcessStep.class);

	protected String locationKey;
	protected String contentKey;
	protected ProcessStep nextStep;

	// used to count remove retries
	private int removeTries = 0;

	public RemoveProcessStep(String locationKey, String contentKey, ProcessStep nexStep) {
		this.locationKey = locationKey;
		this.contentKey = contentKey;
		this.nextStep = nexStep;
	}

	@Override
	public void start() {
		remove(locationKey, contentKey);
	}

	protected void remove(final String locationKey, final String contentKey) {
		this.locationKey = locationKey;
		this.contentKey = contentKey;

		FutureRemove removeFuture = getNetworkManager().remove(locationKey, contentKey);
		removeFuture.addListener(new RemoveListener());
	}

	@Override
	public void rollBack() {
		// TODO re-put removed data
	}

	private void retryRemove() {
		if (++removeTries < H2HConstants.PUT_RETRIES) {
			logger.warn(String
					.format("Remove verification failed. Data is not null. Try #%s. location key = '%s' content key = '%s'",
							removeTries, locationKey, contentKey));
			remove(locationKey, contentKey);
		} else {
			logger.error(String
					.format("Remove verification failed. Data is not null after %s tries. location key = '%s' content key = '%s'",
							removeTries, locationKey, contentKey));
			getProcess().stop(
					String.format("Put verification failed. Data is not null after %s tries.", removeTries));
		}
	}

	private void verifyWithAGet() {
		// get data to verify if everything went correct
		FutureGet getFuture = getNetworkManager().getGlobal(locationKey, contentKey);
		getFuture.addListener(new BaseFutureAdapter<FutureGet>() {
			@Override
			public void operationComplete(FutureGet future) throws Exception {
				// analyze returned data and check if all data objects are empty or null
				for (PeerAddress peeradress : future.getRawData().keySet()) {
					for (Data data : future.getRawData().get(peeradress).values()) {
						if (data != null && data.object() != null) {
							retryRemove();
							return;
						}
					}
				}
				logger.debug(String.format(
						"Verification for remove completed. location key = '%s' content key = '%s'",
						locationKey, contentKey));
				// everything is ok, continue with next step
				getProcess().setNextStep(nextStep);
			}
		});
	}

	/**
	 * Verifies a remove.
	 * 
	 * @author Seppi
	 */
	private class RemoveListener extends BaseFutureAdapter<FutureRemove> {
		@Override
		public void operationComplete(FutureRemove future) throws Exception {
			logger.debug(String.format("Start verification of remove. locationKey = '%s' contentKey = '%s'",
					locationKey, contentKey));
			verifyWithAGet();
		}
	}
}
