package org.hive2hive.core.processes.implementations.common.base;

import java.security.KeyPair;
import java.security.PublicKey;

import net.tomp2p.peers.Number160;

import org.hive2hive.core.exceptions.RemoveFailedException;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.security.H2HEncryptionUtil;

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

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(BaseRemoveProcessStep.class);

	private final IDataManager dataManager;
	protected String locationKey;
	protected String contentKey;
	protected NetworkContent contentToRemove;
	protected KeyPair protectionKey;
	private boolean removePerformed = false;

	public BaseRemoveProcessStep(IDataManager dataManager) {
		this.dataManager = dataManager;
	}

	protected void remove(PublicKey locationKey, String contentKey, NetworkContent contentToRemove,
			KeyPair protectionKey) throws RemoveFailedException {
		remove(H2HEncryptionUtil.key2String(locationKey), contentKey, contentToRemove, protectionKey);
	}

	protected void remove(String locationKey, String contentKey, NetworkContent contentToRemove,
			KeyPair protectionKey) throws RemoveFailedException {
		this.locationKey = locationKey;
		this.contentKey = contentKey;
		this.contentToRemove = contentToRemove;
		this.protectionKey = protectionKey;

		boolean success = false;
		if (this.contentToRemove == null || this.contentToRemove.getVersionKey() == Number160.ZERO) {
			// deletes all versions
			success = dataManager.remove(locationKey, contentKey, protectionKey);
		} else {
			// deletes selected version
			success = dataManager.remove(locationKey, contentKey, this.contentToRemove.getVersionKey(),
					protectionKey);
		}
		removePerformed = true;

		if (!success) {
			throw new RemoveFailedException();
		}
	}

	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {
		if (!removePerformed) {
			logger.info("Noting has been removed. Skip re-adding it to the network");
			return;
		}

		// TODO ugly bug fix
		if (contentToRemove == null) {
			logger.warn(String
					.format("Roll back of remove failed. No content to re-put. location key = '%s' content key = '%s'",
							locationKey, contentKey));
			return;
		}

		boolean success = dataManager.put(locationKey, contentKey, contentToRemove, protectionKey);
		if (success) {
			logger.debug(String.format(
					"Roll back of remove succeeded. location key = '%s' content key = '%s'", locationKey,
					contentKey));
		} else {
			logger.warn(String.format(
					"Roll back of remove failed. Re-put failed. location key = '%s' content key = '%s'",
					locationKey, contentKey));
		}
	}
}
