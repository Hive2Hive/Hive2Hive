package org.hive2hive.core.process.common.put;

import java.security.KeyPair;

import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.network.data.listener.IRemoveListener;
import org.hive2hive.core.process.ProcessStep;

/**
 * A process step which puts a {@link NetworkContent} object under the given keys. </br>
 * <b>Important:</b> Use only this process step to put some data into the network so that in case of failure a
 * appropriate handling is triggered.
 * 
 * @author Seppi
 */
public abstract class BasePutProcessStep extends ProcessStep implements IRemoveListener {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(BasePutProcessStep.class);

	protected String locationKey;
	protected String contentKey;
	protected NetworkContent content;
	protected KeyPair protectionKey;
	private boolean putPerformed = false;

	@Deprecated
	protected void put(String locationKey, String contentKey, NetworkContent content)
			throws PutFailedException {
		put(locationKey, contentKey, content, null);
	}

	protected void put(String locationKey, String contentKey, NetworkContent content, KeyPair protectionKey)
			throws PutFailedException {
		this.locationKey = locationKey;
		this.contentKey = contentKey;
		this.content = content;
		this.protectionKey = protectionKey;

		DataManager dataManager = getNetworkManager().getDataManager();
		if (dataManager == null) {
			getProcess().stop("Node is not connected.");
			throw new PutFailedException();
		}
		boolean success = dataManager.put(locationKey, contentKey, content, protectionKey);
		putPerformed = true;

		if (!success) {
			throw new PutFailedException();
		}
	}

	@Override
	public void rollBack() {
		if (!putPerformed) {
			logger.warn("Nothing to remove at rollback because nothing has been put");
			getProcess().nextRollBackStep();
			return;
		}

		DataManager dataManager = getNetworkManager().getDataManager();
		if (dataManager == null) {
			logger.warn(String
					.format("Roll back of put failed. No connection. location key = '%s' content key = '%s' version key = '%s'",
							locationKey, contentKey, content.getVersionKey()));
			getProcess().nextRollBackStep();
			return;
		}

		dataManager.remove(locationKey, contentKey, content.getVersionKey(), protectionKey, this);
	}

	@Override
	public void onRemoveSuccess() {
		logger.debug(String.format(
				"Roll back of put succeeded. location key = '%s' content key = '%s' version key = '%s'",
				locationKey, contentKey, content.getVersionKey()));
		getProcess().nextRollBackStep();
	}

	@Override
	public void onRemoveFailure() {
		logger.warn(String
				.format("Roll back of put failed. Remove failed. location key = '%s' content key = '%s' version key = '%s'",
						locationKey, contentKey, content.getVersionKey()));
		getProcess().nextRollBackStep();
	}
}
