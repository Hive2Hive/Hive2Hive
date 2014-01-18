package org.hive2hive.processes.implementations.common;

import java.security.KeyPair;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.network.data.listener.IPutListener;
import org.hive2hive.core.network.data.listener.IRemoveListener;
import org.hive2hive.processes.framework.RollbackReason;
import org.hive2hive.processes.framework.abstracts.ProcessStep;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;

public abstract class BasePutProcessStep extends ProcessStep implements IPutListener, IRemoveListener {

	private NetworkManager networkManager;

	public BasePutProcessStep(NetworkManager networkManager) {
		this.networkManager = networkManager;
	}
	
	protected void put(String locationKey, String contentKey, NetworkContent content, KeyPair protectionKey) throws InvalidProcessStateException {

		DataManager dataManager = networkManager.getDataManager();
		if (dataManager == null) {
			cancel(new RollbackReason(this, "Node is not connected."));
		}
		
		dataManager.put(locationKey, contentKey, content, protectionKey, this);
	}

	@Override
	public abstract void onPutSuccess();

	@Override
	public abstract void onPutFailure();

	@Override
	public abstract void onRemoveSuccess();

	@Override
	public abstract void onRemoveFailure();

}
