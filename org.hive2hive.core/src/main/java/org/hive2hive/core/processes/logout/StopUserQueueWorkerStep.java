package org.hive2hive.core.processes.logout;

import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.processframework.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;

public class StopUserQueueWorkerStep extends ProcessStep<Void> {

	private final UserProfileManager userProfileManager;

	public StopUserQueueWorkerStep(UserProfileManager userProfileManager) {
		this.userProfileManager = userProfileManager;
		this.setName(getClass().getName());
	}

	@Override
	protected Void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		userProfileManager.stopQueueWorker();
		setRequiresRollback(true);
		return null;
	}

	@Override
	protected Void doRollback() throws InvalidProcessStateException {
		// restart the queue worker
		userProfileManager.startQueueWorker();
		setRequiresRollback(false);
		return null;
	}
}
