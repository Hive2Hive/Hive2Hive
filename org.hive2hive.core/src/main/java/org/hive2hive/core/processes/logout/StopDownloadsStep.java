package org.hive2hive.core.processes.logout;

import org.hive2hive.core.network.data.download.DownloadManager;
import org.hive2hive.processframework.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;

public class StopDownloadsStep extends ProcessStep<Void> {

	private final DownloadManager downloadManager;

	public StopDownloadsStep(DownloadManager downloadManager) {
		this.downloadManager = downloadManager;
		this.setName(getClass().getName());
	}

	@Override
	protected Void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		downloadManager.stopBackgroundProcesses();
		setRequiresRollback(true);
		return null;
	}

	@Override
	protected Void doRollback() throws InvalidProcessStateException {
		downloadManager.continueBackgroundProcess();
		setRequiresRollback(false);
		return null;
	}
}
