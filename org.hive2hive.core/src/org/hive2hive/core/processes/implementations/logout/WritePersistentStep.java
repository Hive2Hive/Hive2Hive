package org.hive2hive.core.processes.implementations.logout;

import java.io.IOException;
import java.nio.file.Path;

import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;

public class WritePersistentStep extends ProcessStep {

	private final Path root;

	public WritePersistentStep(Path root) {
		this.root = root;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		// TODO Store the Public Keys of the cache as well, read it later at login

		// write the current state to a meta file
		try {
			FileUtil.writePersistentMetaData(root);
		} catch (IOException e) {
			throw new ProcessExecutionException("Meta data could not be persisted.", e);
		}
	}

}
