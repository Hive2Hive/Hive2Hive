package org.hive2hive.core.process.common.userprofiletask;

import net.tomp2p.peers.Number160;

import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.listener.IPutListener;
import org.hive2hive.core.network.data.listener.IRemoveListener;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.common.put.BasePutProcessStep;

public abstract class BasePutUserProfileTask extends ProcessStep implements IPutListener,
		IRemoveListener {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(BasePutProcessStep.class);

	protected String locationKey;
	protected Number160 contentKey;
	protected UserProfileTask userProfileTask;
	protected ProcessStep nextStep;

	public BasePutUserProfileTask(ProcessStep nextStep) {
		this.nextStep = nextStep;
	}

	protected void putUserProfileTask(String locationKey, UserProfileTask userProfileTask) {
		this.locationKey = locationKey;
		// create a content key with a prefix and a time stamp
		this.contentKey = userProfileTask.generateContentKey();
		this.userProfileTask = userProfileTask;

		DataManager dataManager = getNetworkManager().getDataManager();
		if (dataManager == null) {
			getProcess().stop("Node is not connected.");
			return;
		}
		dataManager.putUserProfileTask(locationKey, contentKey, userProfileTask, this);
	}

	@Override
	public void onPutSuccess() {
		getProcess().setNextStep(nextStep);
	}

	@Override
	public void onPutFailure() {
		getProcess().stop("Put failed.");
	}

	@Override
	public void rollBack() {
		DataManager dataManager = getNetworkManager().getDataManager();
		if (dataManager == null) {
			logger.warn(String.format(
					"Roll back of put failed. No connection. location key = '%s' content key = '%s'",
					locationKey, contentKey));
			getProcess().nextRollBackStep();
			return;
		}
		dataManager.removeUserProfileTask(locationKey, contentKey, this);
	}

	@Override
	public void onRemoveSuccess() {
		logger.debug(String.format("Roll back of put succeeded. location key = '%s' content key = '%s'",
				locationKey, contentKey));
		getProcess().nextRollBackStep();
	}

	@Override
	public void onRemoveFailure() {
		logger.warn(String.format(
				"Roll back of put failed. Remove failed. location key = '%s' content key = '%s'",
				locationKey, contentKey));
		getProcess().nextRollBackStep();
	}

}
