package org.hive2hive.core.process.common.remove;

import java.security.KeyPair;

import net.tomp2p.peers.Number160;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.RemoveFailedException;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.process.ProcessStep;

/**
 * A process step which removes a {@link NetworkContent} object under the given keys from the network.</br>
 * <b>Important:</b> Use only this process step to remove data from the network so that in case of failure a
 * appropriate handling is triggered.
 * 
 * @author Seppi
 */
public abstract class BaseRemoveProcessStep extends ProcessStep {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(BaseRemoveProcessStep.class);

	protected String locationKey;
	protected String contentKey;
	protected NetworkContent contentToRemove;
	protected KeyPair protectionKey;
	private boolean removePerformed = false;

	protected void remove(String locationKey, String contentKey, NetworkContent contentToRemove,
			KeyPair protectionKey) throws RemoveFailedException {
		this.locationKey = locationKey;
		this.contentKey = contentKey;
		this.contentToRemove = contentToRemove;
		this.protectionKey = protectionKey;
		DataManager dataManager;
		try {
			dataManager = getNetworkManager().getDataManager();
		} catch (NoPeerConnectionException e) {
			throw new RemoveFailedException("Node is not connected.");
		}

		boolean success = false;
		if (contentToRemove.getVersionKey() == Number160.ZERO) {
			success = dataManager.remove(locationKey, contentKey, protectionKey);
		} else {
			success = dataManager.remove(locationKey, contentKey, contentToRemove.getVersionKey(),
					protectionKey);
		}
		removePerformed = true;

		if (!success) {
			throw new RemoveFailedException();
		}
	}

	@Override
	public void rollBack() {
		if (!removePerformed) {
			logger.info("Noting has been removed. Skip re-adding it to the network");
			getProcess().nextRollBackStep();
			return;
		}

		// TODO ugly bug fix
		if (contentToRemove == null) {
			logger.warn(String
					.format("Roll back of remove failed. No content to re-put. location key = '%s' content key = '%s'",
							locationKey, contentKey));
			getProcess().nextRollBackStep();
			return;
		}

		DataManager dataManager;
		try {
			dataManager = getNetworkManager().getDataManager();
		} catch (NoPeerConnectionException e) {
			logger.warn(String.format(
					"Roll back of remove failed. No connection. location key = '%s' content key = '%s'",
					locationKey, contentKey));
			getProcess().nextRollBackStep();
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

		getProcess().nextRollBackStep();
	}
}
