package org.hive2hive.core.process.common.get;

import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.IGetListener;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.process.ProcessStep;

/**
 * A process step which gets a {@link NetworkContent} object under the given keys. </br>
 * <b>Important:</b> Use only this process step to get some data from the network so that in case of failure a
 * appropriate handling is triggered.
 * 
 * @author Nico
 */
public abstract class BaseGetProcessStep extends ProcessStep implements IGetListener {

	protected String locationKey;
	protected String contentKey;

	public BaseGetProcessStep(String locationKey, String contentKey) {
		this.locationKey = locationKey;
		this.contentKey = contentKey;
	}

	@Override
	public void start() {
		get(locationKey, contentKey);
	}

	protected void get(String locationKey, String contentKey) {
		DataManager dataManager = getNetworkManager().getDataManager();
		if (dataManager == null) {
			getProcess().stop("Node is not connected.");
			return;
		}
		dataManager.getGlobal(locationKey, contentKey, this);
	}

	@Override
	public final void rollBack() {
		// ignore because just a get
	}

	protected abstract void handleGetResult(NetworkContent content);

	@Override
	public void onSuccess(NetworkContent content) {
		handleGetResult(content);
	}

	@Override
	public void onFailure() {
		getProcess().stop("Get failed.");
	}
}
