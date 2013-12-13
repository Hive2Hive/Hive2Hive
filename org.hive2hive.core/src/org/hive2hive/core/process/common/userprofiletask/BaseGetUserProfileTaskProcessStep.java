package org.hive2hive.core.process.common.userprofiletask;

import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.listener.IGetListener;
import org.hive2hive.core.process.ProcessStep;

public abstract class BaseGetUserProfileTaskProcessStep extends ProcessStep implements IGetListener {

	protected void getUserProfileTask(String locationKey) {
		DataManager dataManager = getNetworkManager().getDataManager();
		if (dataManager == null) {
			getProcess().stop("Node is not connected.");
			return;
		}
		dataManager.getUserProfileTask(locationKey, this);
	}

	@Override
	public final void rollBack() {
		// ignore because just a get
		getProcess().nextRollBackStep();
	}

}
