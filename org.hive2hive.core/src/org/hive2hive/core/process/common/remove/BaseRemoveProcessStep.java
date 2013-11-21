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
public abstract class BaseRemoveProcessStep extends ProcessStep implements IRemoveListener{

	protected String locationKey;
	protected String contentKey;
	protected NetworkContent contentToRemove;
	protected ProcessStep nextStep;

	public BaseRemoveProcessStep(ProcessStep nexStep) {
		this.nextStep = nexStep;
	}

	protected void remove(String locationKey, String contentKey, NetworkContent contentToRemove) {
		this.locationKey = locationKey;
		this.contentKey = contentKey;
		this.contentToRemove = contentToRemove;
		DataManager dataManager = getNetworkManager().getDataManager();
		if (dataManager == null) {
			getProcess().stop("Node is not connected.");
			return;
		}
		dataManager.remove(locationKey, contentKey, contentToRemove.getVersionKey(), this);
	}

	@Override
	public void rollBack() {
		// TODO ugly bug fix
		if (contentToRemove == null)
			return;
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
