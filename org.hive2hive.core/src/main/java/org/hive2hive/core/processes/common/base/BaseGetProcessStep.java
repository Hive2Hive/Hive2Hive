package org.hive2hive.core.processes.common.base;

import java.security.PublicKey;

import org.hive2hive.core.model.BaseNetworkContent;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.parameters.IParameters;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.security.H2HDefaultEncryption;
import org.hive2hive.processframework.abstracts.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;

/**
 * Abstract class for {@link ProcessStep}s that intend to GET content from the DHT.
 * 
 * @author Christian
 * 
 */
public abstract class BaseGetProcessStep extends ProcessStep {

	protected final DataManager dataManager;

	public BaseGetProcessStep(DataManager dataManager) {
		this.dataManager = dataManager;
	}

	protected BaseNetworkContent get(PublicKey locationKey, String contentKey) throws InvalidProcessStateException {
		return get(H2HDefaultEncryption.key2String(locationKey), contentKey);
	}

	protected BaseNetworkContent get(String locationKey, String contentKey) throws InvalidProcessStateException {
		IParameters parameters = new Parameters().setLocationKey(locationKey).setContentKey(contentKey);
		return dataManager.get(parameters);
	}

}
