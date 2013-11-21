package org.hive2hive.core.process.common.put;

import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.IPutListener;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.process.ProcessStep;

/**
 * A process step which puts a {@link NetworkContent} object under the given keys. </br>
 * <b>Important:</b> Use only this process step to put some data into the network so that in case of failure a
 * appropriate handling is triggered.
 * 
 * @author Seppi
 */
public class PutProcessStep extends ProcessStep implements IPutListener {

	protected String locationKey;
	protected String contentKey;
	protected NetworkContent content;
	protected ProcessStep nextStep;

	public PutProcessStep(String locationKey, String contentKey, NetworkContent content, ProcessStep nextStep) {
		this.locationKey = locationKey;
		this.contentKey = contentKey;
		this.content = content;
		this.nextStep = nextStep;
	}

	@Override
	public void start() {
		put(locationKey, contentKey, content);
	}
	
	protected void put(String locationKey, String contentKey, NetworkContent content) {
		DataManager dataManager = getNetworkManager().getDataManager();
		if (dataManager == null) {
			getProcess().stop("Node is not connected.");
			return;
		}
		dataManager.putGlobal(locationKey, contentKey, content, this);
	}

	@Override
	public void rollBack() {
		DataManager dataManager = getNetworkManager().getDataManager();
		if (dataManager == null) {
			return;
		}
		dataManager.remove(locationKey, contentKey, content.getVersionKey()).awaitUninterruptibly();
	}

	@Override
	public void onSuccess() {
		getProcess().setNextStep(nextStep);
	}

	@Override
	public void onFailure() {
		getProcess().stop("Put failed.");
	}
}
