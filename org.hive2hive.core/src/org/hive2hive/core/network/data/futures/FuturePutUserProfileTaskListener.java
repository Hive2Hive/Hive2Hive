package org.hive2hive.core.network.data.futures;

import java.util.ArrayList;
import java.util.List;

import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.FuturePut;
import net.tomp2p.futures.FutureRemove;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.peers.PeerAddress;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.H2HStorageMemory.PutStatusH2H;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.listener.IPutUserProfileTaskListener;
import org.hive2hive.core.network.usermessages.UserProfileTask;

/**
 * A put future adapter for verifying a put of a {@link UserProfileTask} object. Provides failure handling and
 * notifying {@link IPutUserProfileTaskListener} listeners. In case of a successful put
 * {@link IPutUserProfileTaskListener#onSuccess()} gets called. In case of a failed put
 * {@link IPutUserProfileTaskListener#onFailure()} gets called. </br></br>
 * <b>Failure Handling</b></br>
 * Putting can fail when the future object failed, when the future object contains wrong data or the
 * responding node detected a failure. See {@link PutStatusH2H} for possible failures. If putting fails the
 * adapter retries it to a certain threshold (see {@link H2HConstants.PUT_RETRIES}).
 * 
 * @author Seppi
 */
public class FuturePutUserProfileTaskListener extends BaseFutureAdapter<FuturePut> {

	private final static Logger logger = H2HLoggerFactory.getLogger(FuturePutUserProfileTaskListener.class);

	private final String locationKey;
	private final Number160 contentKey;
	private final UserProfileTask userProfileTask;
	private final IPutUserProfileTaskListener listener;
	private final DataManager dataManager;

	// used to count put retries
	private int putTries = 0;

	public FuturePutUserProfileTaskListener(String locationKey, Number160 contentKey,
			UserProfileTask userProfileTask, IPutUserProfileTaskListener listener, DataManager dataManager) {
		this.locationKey = locationKey;
		this.contentKey = contentKey;
		this.userProfileTask = userProfileTask;
		this.listener = listener;
		this.dataManager = dataManager;
	}

	@Override
	public void operationComplete(FuturePut future) throws Exception {
		logger.debug(String.format(
				"Start verification of put user profile task. location key = '%s' content key = '%s'",
				locationKey, contentKey));

		if (future.isFailed()) {
			logger.warn(String
					.format("Put user profile task future was not successful. location key = '%s' content key = '%s'",
							locationKey, contentKey));
			retryPut();
			return;
		} else if (future.getRawResult().isEmpty()) {
			logger.warn(String
					.format("Put user profile task future's raw results are empty. location key = '%s' content key = '%s'",
							locationKey, contentKey));
			retryPut();
			return;
		}

		// analyze returned put status
		List<PeerAddress> versionConflict = new ArrayList<PeerAddress>();
		List<PeerAddress> fail = new ArrayList<PeerAddress>();
		for (PeerAddress peeradress : future.getRawResult().keySet()) {
			for (Number640 key : future.getRawResult().get(peeradress).keySet()) {
				byte status = future.getRawResult().get(peeradress).get(key);
				switch (PutStatusH2H.values()[status]) {
					case OK:
						break;
					case FAILED:
					case FAILED_NOT_ABSENT:
					case FAILED_SECURITY:
						logger.warn(String
								.format("A node denied putting data. reason = '%s' location key = '%s' content key = '%s'",
										PutStatusH2H.values()[status], locationKey, contentKey));
						fail.add(peeradress);
						break;
					case VERSION_CONFLICT:
					case VERSION_CONFLICT_NO_BASED_ON:
					case VERSION_CONFLICT_NO_VERSION_KEY:
					case VERSION_CONFLICT_OLD_TIMESTAMP:
						logger.warn(String
								.format("A version conflict detected. reason = '%s' location key = '%s' content key = '%s'",
										PutStatusH2H.values()[status], locationKey, contentKey));
						versionConflict.add(peeradress);
						break;
				}
			}
		}

		if (!versionConflict.isEmpty()) {
			// remove succeeded puts
			dataManager.removeUserProfileTask(locationKey, contentKey).addListener(new RemoveListener());
		} else if ((double) fail.size() < ((double) future.getRawResult().size()) / 2.0) {
			// majority of the contacted nodes responded with ok
			if (listener != null)
				listener.onPutUserProfileTaskSuccess();
		} else {
			logger.warn(String.format("%s of %s contacted nodes failed.", fail.size(), future.getRawResult()
					.size()));
			retryPut();
		}
	}

	/**
	 * Retries a put till a certain threshold is reached (see {@link H2HConstants.PUT_RETRIES}). Removes first
	 * the possibly succeeded puts. A {@link RetryPutListener} tries to put again the given content.
	 */
	private void retryPut() {
		if (putTries++ < H2HConstants.PUT_RETRIES) {
			logger.warn(String.format(
					"Put user profile task retry #%s. location key = '%s' content key = '%s'", putTries,
					locationKey, contentKey));
			// remove succeeded puts
			dataManager.removeUserProfileTask(locationKey, contentKey).addListener(
					new RetryPutRemoveListener());
		} else {
			logger.error(String
					.format("Put verification of user profile task failed. Couldn't put data after %s tries. location key = '%s' content key = '%s'",
							putTries - 1, locationKey, contentKey));
			// remove succeeded puts
			dataManager.removeUserProfileTask(locationKey, contentKey).addListener(new RemoveListener());
		}
	}

	private class RetryPutRemoveListener extends BaseFutureAdapter<FutureRemove> {
		@Override
		public void operationComplete(FutureRemove future) throws Exception {
			if (future.isFailed())
				logger.warn(String
						.format("Put Retry: Could not delete the newly put content. location key = '%s' content key = '%s'",
								locationKey, contentKey));
			dataManager.putUserProfileTask(locationKey, contentKey, userProfileTask).addListener(
					FuturePutUserProfileTaskListener.this);
		}
	}

	private class RemoveListener extends BaseFutureAdapter<FutureRemove> {
		@Override
		public void operationComplete(FutureRemove future) throws Exception {
			if (listener != null)
				listener.onPutUserProfileTaskFailure();
		}
	}

}
