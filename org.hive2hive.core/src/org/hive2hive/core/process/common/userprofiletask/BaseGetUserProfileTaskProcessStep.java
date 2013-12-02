package org.hive2hive.core.process.common.userprofiletask;

import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.listener.IGetUserProfileTaskListener;
import org.hive2hive.core.process.ProcessStep;

public abstract class BaseGetUserProfileTaskProcessStep extends ProcessStep implements IGetUserProfileTaskListener {

	protected void getUserProfileTask(String locationKey) {
		DataManager dataManager = getNetworkManager().getDataManager();
		if (dataManager == null) {
			getProcess().stop("Node is not connected.");
			return;
		}
		dataManager.getNextUserProfileTask(locationKey, this);
	}

	@Override
	public final void rollBack() {
		// ignore because just a get
		getProcess().nextRollBackStep();
	}

}
