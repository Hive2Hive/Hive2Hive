package org.hive2hive.core.process.common.remove;

import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.IRemoveListener;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.process.ProcessStep;

/**
 * A process step which removes a {@link NetworkContent} object under the given keys from the network.</br>
 * <b>Important:</b> Use only this process step to remove data from the network so that in case of failure a
 * appropriate handling is triggered.
 * 
 * @author Seppi
 */
public class RemoveProcessStep extends ProcessStep implements IRemoveListener{

	protected final String locationKey;
	protected final String contentKey;
	protected final NetworkContent contentToRemove;
	protected ProcessStep nextStep;

	public RemoveProcessStep(String locationKey, String contentKey, NetworkContent contentToRemove, ProcessStep nexStep) {
		this.locationKey = locationKey;
		this.contentKey = contentKey;
		this.contentToRemove = contentToRemove;
		this.nextStep = nexStep;
	}

	@Override
	public void start() {
		remove(locationKey, contentKey, contentToRemove);
	}

	protected void remove(String locationKey, String contentKey, NetworkContent contentToRemove) {
		DataManager dataManager = getNetworkManager().getDataManager();
		if (dataManager == null) {
			getProcess().stop("Node is not connected.");
			return;
		}
		dataManager.remove(locationKey, contentKey, contentToRemove.getVersionKey(), this);
	}

	@Override
	public void rollBack() {
		DataManager dataManager = getNetworkManager().getDataManager();
		if (dataManager == null) {
			return;
		}
		dataManager.putGlobal(locationKey, contentKey, contentToRemove).awaitUninterruptibly();
	}

	@Override
	public void onSuccess() {
		getProcess().setNextStep(nextStep);
	}

	@Override
	public void onFailure() {
		getProcess().stop("Remove failed.");
	}

}
