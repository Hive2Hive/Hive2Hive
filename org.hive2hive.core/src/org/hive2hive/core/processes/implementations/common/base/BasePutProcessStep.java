package org.hive2hive.core.processes.implementations.common.base;

import java.security.KeyPair;
import java.security.PublicKey;

import org.hive2hive.core.exceptions.InvalidProcessStateException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.security.H2HEncryptionUtil;

public abstract class BasePutProcessStep extends ProcessStep {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(BasePutProcessStep.class);

	private final IDataManager dataManager;
	protected boolean putPerformed;

	private String locationKey;
	private String contentKey;
	private KeyPair protectionKey;
	private NetworkContent content;

	public BasePutProcessStep(IDataManager dataManager) {
		this.dataManager = dataManager;
	}

	protected void put(PublicKey locationKey, String contentKey, NetworkContent content, KeyPair protectionKey)
			throws PutFailedException {
		put(H2HEncryptionUtil.key2String(locationKey), contentKey, content, protectionKey);
	}

	protected void put(String locationKey, String contentKey, NetworkContent content, KeyPair protectionKey)
			throws PutFailedException {
		this.locationKey = locationKey;
		this.contentKey = contentKey;
		this.content = content;
		this.protectionKey = protectionKey;

		boolean success = dataManager.put(locationKey, contentKey, content, protectionKey);
		putPerformed = true;

		if (!success) {
			throw new PutFailedException();
		}
	}

	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {
		if (!putPerformed) {
			logger.warn("Nothing to remove at rollback because nothing has been put.");
			return;
		}

		boolean success = dataManager.remove(locationKey, contentKey, content.getVersionKey(), protectionKey);
		if (success) {
			logger.debug(String.format(
					"Rollback of put succeeded. location key = '%s' content key = '%s' version key = '%s'",
					locationKey, contentKey, content.getVersionKey()));
		} else {
			logger.warn(String
					.format("Rollback of put failed. Remove failed. location key = '%s' content key = '%s' version key = '%s'",
							locationKey, contentKey, content.getVersionKey()));
		}
	}

}
