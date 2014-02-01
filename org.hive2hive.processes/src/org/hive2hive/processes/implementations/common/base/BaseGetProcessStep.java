package org.hive2hive.processes.implementations.common.base;

import java.security.PublicKey;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.processes.framework.RollbackReason;
import org.hive2hive.processes.framework.abstracts.ProcessStep;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;

public abstract class BaseGetProcessStep extends ProcessStep {

	protected final NetworkManager networkManager;

	public BaseGetProcessStep(NetworkManager networkManager) {
		this.networkManager = networkManager;
	}

	protected NetworkContent get(PublicKey locationKey, String contentKey)
			throws InvalidProcessStateException {
		return get(H2HEncryptionUtil.key2String(locationKey), contentKey);
	}

	protected NetworkContent get(String locationKey, String contentKey) throws InvalidProcessStateException {
		DataManager dataManager;
		try {
			dataManager = networkManager.getDataManager();
			return dataManager.get(locationKey, contentKey);
		} catch (NoPeerConnectionException e) {
			cancel(new RollbackReason(this, "Node is not connected."));
			return null;
		}
	}

}
