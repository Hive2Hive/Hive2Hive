package org.hive2hive.core.processes.implementations.common.base;

import java.security.PublicKey;

import org.hive2hive.core.exceptions.InvalidProcessStateException;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.security.H2HEncryptionUtil;

public abstract class BaseGetProcessStep extends ProcessStep {

	protected final IDataManager dataManager;

	public BaseGetProcessStep(IDataManager dataManager) {
		this.dataManager = dataManager;
	}

	protected NetworkContent get(PublicKey locationKey, String contentKey)
			throws InvalidProcessStateException {
		return get(H2HEncryptionUtil.key2String(locationKey), contentKey);
	}

	protected NetworkContent get(String locationKey, String contentKey) throws InvalidProcessStateException {
		return dataManager.get(locationKey, contentKey);
	}

}
