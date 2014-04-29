package org.hive2hive.core.processes.implementations.common.base;

import java.security.PublicKey;

import org.hive2hive.core.model.NetworkContent;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.network.data.parameters.IParameters;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.security.H2HEncryptionUtil;

/**
 * Abstract class for {@link ProcessStep}s that intend to GET content from the DHT.
 * 
 * @author Christian
 * 
 */
public abstract class BaseGetProcessStep extends ProcessStep {

	private final IDataManager dataManager;

	public BaseGetProcessStep(IDataManager dataManager) {
		this.dataManager = dataManager;
	}

	protected NetworkContent get(PublicKey locationKey, String contentKey)
			throws InvalidProcessStateException {
		return get(H2HEncryptionUtil.key2String(locationKey), contentKey);
	}

	protected NetworkContent get(String locationKey, String contentKey) throws InvalidProcessStateException {
		IParameters parameters = new Parameters().setLocationKey(locationKey).setContentKey(contentKey);
		return dataManager.get(parameters);
	}

}
