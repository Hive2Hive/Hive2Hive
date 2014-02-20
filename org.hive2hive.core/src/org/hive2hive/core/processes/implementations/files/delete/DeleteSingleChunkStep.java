package org.hive2hive.core.processes.implementations.files.delete;

import java.security.KeyPair;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.RemoveFailedException;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.common.base.BaseRemoveProcessStep;

public class DeleteSingleChunkStep extends BaseRemoveProcessStep {

	private final String locationKey;
	private final KeyPair protectionKeys;

	public DeleteSingleChunkStep(String locationKey, KeyPair protectionKeys, IDataManager dataManager) {
		super(dataManager);
		this.locationKey = locationKey;
		this.protectionKeys = protectionKeys;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		try {
			remove(locationKey, H2HConstants.FILE_CHUNK, null, protectionKeys);
		} catch (RemoveFailedException e) {
			throw new ProcessExecutionException("Removal of chunk failed.", e);
		}
	}

}