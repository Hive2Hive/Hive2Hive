package org.hive2hive.core.processes.implementations.files.delete;

import java.security.KeyPair;
import java.security.PublicKey;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.InvalidProcessStateException;
import org.hive2hive.core.exceptions.RemoveFailedException;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.implementations.common.base.BaseRemoveProcessStep;

public class DeleteSingleChunkStep extends BaseRemoveProcessStep {

	private final PublicKey locationKey;
	private final KeyPair protectionKeys;

	public DeleteSingleChunkStep(PublicKey locationKey, KeyPair protectionKeys, IDataManager dataManager) {
		super(dataManager);
		this.locationKey = locationKey;
		this.protectionKeys = protectionKeys;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		try {
			remove(locationKey, H2HConstants.FILE_CHUNK, null, protectionKeys);
		} catch (RemoveFailedException e) {
			cancel(new RollbackReason(this, "Remove of chunk failed."));
		}
	}

}
