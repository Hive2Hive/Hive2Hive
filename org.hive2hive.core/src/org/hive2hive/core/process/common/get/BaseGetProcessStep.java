package org.hive2hive.core.process.common.get;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.process.ProcessStep;

/**
 * A process step which gets a {@link NetworkContent} object under the given keys. </br>
 * <b>Important:</b> Use only this process step to get some data from the network so that in case of failure a
 * appropriate handling is triggered.
 * 
 * @author Nico, Seppi
 */
public abstract class BaseGetProcessStep extends ProcessStep {

	protected NetworkContent get(String locationKey, String contentKey) {
		DataManager dataManager;
		try {
			dataManager = getNetworkManager().getDataManager();
		} catch (NoPeerConnectionException e) {
			getProcess().stop("Node is not connected.");
			return null;
		}
		return dataManager.get(locationKey, contentKey);
	}

	@Override
	public void rollBack() {
		// ignore because read-only
		getProcess().nextRollBackStep();
	}

}
