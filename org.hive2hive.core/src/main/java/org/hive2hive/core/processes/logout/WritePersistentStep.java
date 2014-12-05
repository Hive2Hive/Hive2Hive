package org.hive2hive.core.processes.logout;

import java.io.IOException;

import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.file.IFileAgent;
import org.hive2hive.core.network.data.PublicKeyManager;
import org.hive2hive.core.network.data.download.DownloadManager;
import org.hive2hive.core.security.IH2HSerialize;
import org.hive2hive.processframework.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;

public class WritePersistentStep extends ProcessStep<Void> {

	private final IFileAgent fileAgent;
	private final PublicKeyManager keyManager;
	private final DownloadManager downloadManager;
	private final IH2HSerialize serializer;

	public WritePersistentStep(IFileAgent fileAgent, PublicKeyManager keyManager, DownloadManager downloadManager,
			IH2HSerialize serializer) {
		this.serializer = serializer;
		this.fileAgent = fileAgent;
		this.keyManager = keyManager;
		this.downloadManager = downloadManager;
		this.setName(getClass().getName());
	}

	@Override
	protected Void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		// write the current state to a meta file
		try {
			FileUtil.writePersistentMetaData(fileAgent, keyManager, downloadManager, serializer);
		} catch (IOException ex) {
			throw new ProcessExecutionException(this, ex, "Meta data could not be persisted.");
		}
		return null;
	}

}
