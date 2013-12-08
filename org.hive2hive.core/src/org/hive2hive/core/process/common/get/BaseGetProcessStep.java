package org.hive2hive.core.process.common.get;

import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.network.data.listener.IGetListener;
import org.hive2hive.core.process.ProcessStep;

/**
 * A process step which gets a {@link NetworkContent} object under the given keys. </br>
 * <b>Important:</b> Use only this process step to get some data from the network so that in case of failure a
 * appropriate handling is triggered.
 * 
 * @author Nico, Seppi
 */
public abstract class BaseGetProcessStep extends ProcessStep implements IGetListener {

	protected void get(String locationKey, String contentKey) {
		DataManager dataManager = getNetworkManager().getDataManager();
		if (dataManager == null) {
			getProcess().stop("Node is not connected.");
			return;
		}
		dataManager.getGlobal(locationKey, contentKey, this);
	}

	@Override
	public void rollBack() {
		// ignore because read-only
		getProcess().nextRollBackStep();
	}

}
