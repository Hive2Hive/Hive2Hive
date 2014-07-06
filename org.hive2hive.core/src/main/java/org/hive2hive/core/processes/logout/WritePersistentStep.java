package org.hive2hive.core.processes.logout;

import java.io.IOException;
import java.nio.file.Path;

import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.network.data.PublicKeyManager;
import org.hive2hive.core.network.data.download.DownloadManager;
import org.hive2hive.processframework.abstracts.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;

public class WritePersistentStep extends ProcessStep {

	private final Path root;
	private final PublicKeyManager keyManager;
	private final DownloadManager downloadManager;

	public WritePersistentStep(Path root, PublicKeyManager keyManager, DownloadManager downloadManager) {
		this.root = root;
		this.keyManager = keyManager;
		this.downloadManager = downloadManager;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		// write the current state to a meta file
		try {
			FileUtil.writePersistentMetaData(root, keyManager, downloadManager);
		} catch (IOException e) {
			throw new ProcessExecutionException("Meta data could not be persisted.", e);
		}
	}

}
