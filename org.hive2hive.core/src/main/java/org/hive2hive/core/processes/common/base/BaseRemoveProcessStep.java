package org.hive2hive.core.processes.common.base;

import java.security.KeyPair;
import java.security.PublicKey;

import org.hive2hive.core.exceptions.RemoveFailedException;
import org.hive2hive.core.model.NetworkContent;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.network.data.parameters.IParameters;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.security.H2HDefaultEncryption;
import org.hive2hive.processframework.RollbackReason;
import org.hive2hive.processframework.abstracts.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A process step which removes a {@link NetworkContent} object under the given keys from the network.</br>
 * <b>Important:</b> Use only this process step to remove data from the network so that in case of failure a
 * appropriate handling is triggered.
 * 
 * @author Seppi, Nico
 */
public abstract class BaseRemoveProcessStep extends ProcessStep {

	// TODO this class needs to be refactored
	// TODO this class is only rollbacking the last execution, however there are steps that execute remove()
	// multiple times. Make sure, that a single step only calls remove() once. Otherwise, create multiple
	// steps! (e.g. DeleteSingleChunkStep)

	private static final Logger logger = LoggerFactory.getLogger(BaseRemoveProcessStep.class);

	private IParameters parameters;
	private final IDataManager dataManager;
	private boolean removePerformed = false;

	public BaseRemoveProcessStep(IDataManager dataManager) {
		this.dataManager = dataManager;
	}

	protected void remove(PublicKey locationKey, String contentKey, KeyPair protectionKey)
			throws RemoveFailedException {
		remove(H2HDefaultEncryption.key2String(locationKey), contentKey, protectionKey);
	}

	protected void remove(String locationKey, String contentKey, KeyPair protectionKey)
			throws RemoveFailedException {
		parameters = new Parameters().setLocationKey(locationKey).setContentKey(contentKey)
				.setProtectionKeys(protectionKey);

		// deletes all versions
		boolean success = dataManager.remove(parameters);
		removePerformed = true;

		if (!success) {
			throw new RemoveFailedException();
		}
	}

	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {
		if (!removePerformed) {
			logger.info("Noting has been removed. Skip re-adding it to the network.");
			return;
		}

		// TODO ugly bug fix
		if (parameters.getData() == null) {
			logger.warn("Rollback of remove failed. No content to re-put. '{}'", parameters.toString());
			return;
		}

		boolean success = dataManager.put(parameters);
		if (success) {
			logger.debug("Rollback of remove succeeded. '{}'", parameters.toString());
		} else {
			logger.warn("Rollback of remove failed. Re-put failed. '{}'", parameters.toString());
		}
	}
}
