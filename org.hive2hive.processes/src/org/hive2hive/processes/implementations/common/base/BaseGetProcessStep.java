package org.hive2hive.processes.implementations.common.base;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.network.data.listener.IGetListener;
import org.hive2hive.processes.framework.RollbackReason;
import org.hive2hive.processes.framework.abstracts.ProcessStep;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;

public abstract class BaseGetProcessStep extends ProcessStep implements IGetListener {

	private final NetworkManager networkManager;

	public BaseGetProcessStep(NetworkManager networkManager) {
		this.networkManager = networkManager;
	}
	
	protected void get(String locationKey, String contentKey) throws InvalidProcessStateException {
		
		DataManager dataManager = networkManager.getDataManager();
		if (dataManager == null) {
			cancel(new RollbackReason(this, "Node is not connected."));
		}
		
		dataManager.get(locationKey, contentKey, this);
	}

	@Override
	public abstract void handleGetResult(NetworkContent content);

}
