package org.hive2hive.core.processes.implementations.logout;

import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;

public class WritePersistentStep extends ProcessStep {

	private final FileManager fileManager;

	public WritePersistentStep(FileManager fileManager) {
		this.fileManager = fileManager;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		// TODO Store the Public Keys of the cache as well, read it later at login

		// write the current state to a meta file
		fileManager.writePersistentMetaData();
	}

}
