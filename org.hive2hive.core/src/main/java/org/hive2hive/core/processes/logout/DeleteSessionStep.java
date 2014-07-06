package org.hive2hive.core.processes.logout;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.processframework.RollbackReason;
import org.hive2hive.processframework.abstracts.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;

/**
 * Remove the session from the node
 * 
 * @author Nico
 * 
 */
public class DeleteSessionStep extends ProcessStep {

	private final NetworkManager networkManager;
	private H2HSession session; // backup

	public DeleteSessionStep(NetworkManager networkManager) {
		this.networkManager = networkManager;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		try {
			session = networkManager.getSession();
			networkManager.setSession(null);
		} catch (NoSessionException e) {
			// session already deleted
		}
	}

	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {
		// restore the session
		networkManager.setSession(session);
	}

}
