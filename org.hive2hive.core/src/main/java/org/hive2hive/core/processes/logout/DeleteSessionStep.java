package org.hive2hive.core.processes.logout;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.processframework.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;

/**
 * Remove the session from the node
 * 
 * @author Nico
 * 
 */
public class DeleteSessionStep extends ProcessStep<Void> {

	private final NetworkManager networkManager;
	private H2HSession session; // backup

	public DeleteSessionStep(NetworkManager networkManager) {
		this.setName(getClass().getName());
		this.networkManager = networkManager;
	}

	@Override
	protected Void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		try {
			session = networkManager.getSession();

			// stop all session components
			session.getDownloadManager().stopBackgroundProcesses();
			session.getProfileManager().stopQueueWorker();

			networkManager.setSession(null);
			setRequiresRollback(true);
		} catch (NoSessionException e) {
			// session already deleted
		}
		return null;
	}

	@Override
	protected Void doRollback() throws InvalidProcessStateException {
		// restore the session
		networkManager.setSession(session);

		// restart the queue worker
		session.getProfileManager().startQueueWorker();

		setRequiresRollback(false);
		return null;
	}

}
