package org.hive2hive.core.process.common.userprofiletask;

import net.tomp2p.peers.Number160;

import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.listener.IPutUserProfileTaskListener;
import org.hive2hive.core.network.data.listener.IRemoveUserProfileTaskListener;
import org.hive2hive.core.network.usermessages.UserProfileTask;
import org.hive2hive.core.process.ProcessStep;

public abstract class BaseRemoveUserProfileTaskProcessStep extends ProcessStep implements
		IRemoveUserProfileTaskListener, IPutUserProfileTaskListener {

	private static final H2HLogger logger = H2HLoggerFactory
			.getLogger(BaseRemoveUserProfileTaskProcessStep.class);

	protected String locationKey;
	protected Number160 contentKey;
	protected UserProfileTask userProfileTaskToRemove;
	protected ProcessStep nextStep;

	public BaseRemoveUserProfileTaskProcessStep(ProcessStep nexStep) {
		this.nextStep = nexStep;
	}

	protected void remove(String locationKey, Number160 contentKey, UserProfileTask userProfileTaskToRemove) {
		this.locationKey = locationKey;
		this.contentKey = contentKey;
		// needed for roll back
		this.userProfileTaskToRemove = userProfileTaskToRemove;
		DataManager dataManager = getNetworkManager().getDataManager();
		if (dataManager == null) {
			getProcess().stop("Node is not connected.");
			return;
		}
		dataManager.removeUserProfileTask(locationKey, contentKey, this);
	}

	@Override
	public void onRemoveUserProfileTaskSuccess() {
		getProcess().setNextStep(nextStep);
	}

	@Override
	public void onRemoveUserProfileTaskFailure() {
		getProcess().stop("Remove failed.");
	}

	@Override
	public void rollBack() {
		// TODO ugly bug fix
		if (userProfileTaskToRemove == null) {
			logger.warn(String
					.format("Roll back of remove failed. No content to re-put. location key = '%s' content key = '%s'",
							locationKey, contentKey));
			getProcess().nextRollBackStep();
			return;
		}

		DataManager dataManager = getNetworkManager().getDataManager();
		if (dataManager == null) {
			logger.warn(String.format(
					"Roll back of remove failed. No connection. location key = '%s' content key = '%s'",
					locationKey, contentKey));
			getProcess().nextRollBackStep();
			return;
		}
		dataManager.putUserProfileTask(locationKey, contentKey, userProfileTaskToRemove, this);
	}

	@Override
	public void onPutUserProfileTaskSuccess() {
		logger.debug(String.format("Roll back of remove succeeded. location key = '%s' content key = '%s'",
				locationKey, contentKey));
		getProcess().nextRollBackStep();
	}

	@Override
	public void onPutUserProfileTaskFailure() {
		logger.warn(String.format(
				"Roll back of remove failed. Re-put failed. location key = '%s' content key = '%s'",
				locationKey, contentKey));
		getProcess().nextRollBackStep();
	}

}
