package org.hive2hive.core.processes.common.base;

import java.security.KeyPair;
import java.security.PublicKey;

import org.hive2hive.core.exceptions.RemoveFailedException;
import org.hive2hive.core.model.BaseNetworkContent;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.DataManager.H2HPutStatus;
import org.hive2hive.core.network.data.parameters.IParameters;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.security.H2HDefaultEncryption;
import org.hive2hive.processframework.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessRollbackException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A process step which removes a {@link BaseNetworkContent} object under the given keys from the network.</br>
 * <b>Important:</b> Use only this process step to remove data from the network so that in case of failure a
 * appropriate handling is triggered.
 * 
 * @author Seppi, Nico
 */
public abstract class BaseRemoveProcessStep extends ProcessStep<Void> {

	// TODO this class needs to be refactored
	// TODO this class is only rollbacking the last execution, however there are steps that execute remove()
	// multiple times. Make sure, that a single step only calls remove() once. Otherwise, create multiple
	// steps! (e.g. DeleteSingleChunkStep)

	private static final Logger logger = LoggerFactory.getLogger(BaseRemoveProcessStep.class);

	private IParameters parameters;
	private final DataManager dataManager;

	public BaseRemoveProcessStep(DataManager dataManager) {
		this.dataManager = dataManager;
	}

	protected void remove(PublicKey locationKey, String contentKey, KeyPair protectionKey) throws RemoveFailedException {
		remove(H2HDefaultEncryption.key2String(locationKey), contentKey, protectionKey);
	}

	protected void remove(String locationKey, String contentKey, KeyPair protectionKey) throws RemoveFailedException {
		parameters = new Parameters().setLocationKey(locationKey).setContentKey(contentKey).setProtectionKeys(protectionKey);

		// deletes all versions
		boolean success = dataManager.remove(parameters);
		setRequiresRollback(true);

		if (!success) {
			throw new RemoveFailedException();
		}
	}

	@Override
	protected Void doRollback() throws InvalidProcessStateException, ProcessRollbackException {

		// TODO ugly bug fix
		if (parameters.getNetworkContent() == null) {
			throw new ProcessRollbackException(this, String.format("Rollback of remove failed. No content to re-put. Parameters: '%s'.", parameters.toString()));
		}

		H2HPutStatus status = dataManager.put(parameters);
		if (status.equals(H2HPutStatus.OK)) {
			logger.debug("Rollback of remove succeeded. '{}'", parameters.toString());
			setRequiresRollback(false);
		} else {
			throw new ProcessRollbackException(this, String.format("Rollback of remove failed. Re-put failed. Parameters: '%s'.", parameters.toString()));
		}
		
		return null;
	}
}
