package org.hive2hive.processes.implementations.login;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.login.SessionParameters;
import org.hive2hive.processes.framework.RollbackReason;
import org.hive2hive.processes.framework.abstracts.ProcessStep;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.processes.implementations.context.LoginProcessContext;

public class SessionCreationStep extends ProcessStep {

	private final SessionParameters params;
	private final LoginProcessContext context;
	private final NetworkManager networkManager;

	public SessionCreationStep(SessionParameters params, LoginProcessContext context,
			NetworkManager networkManager) {
		this.params = params;
		this.context = context;
		this.networkManager = networkManager;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		// create session
		params.setKeyPair(context.consumeUserProfile().getEncryptionKeys());
		H2HSession session = new H2HSession(params);

		// set session
		networkManager.setSession(session);
	}

	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {

		// invalidate the session
		networkManager.setSession(null);
	}

}
