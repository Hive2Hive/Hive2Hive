package org.hive2hive.core.processes.implementations.logout;

import org.hive2hive.core.network.data.download.DownloadManager;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;

public class StopDownloadsStep extends ProcessStep {

	private final DownloadManager downloadManager;

	public StopDownloadsStep(DownloadManager downloadManager) {
		this.downloadManager = downloadManager;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		downloadManager.stopBackgroundProcesses();
	}

	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {
		downloadManager.continueBackgroundProcess();
	}
}
