package org.hive2hive.core.processes.implementations.login;

import java.io.IOException;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.PublicKeyManager;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.context.LoginProcessContext;

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
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		UserProfile userProfile = context.consumeUserProfile();

		H2HSession session;
		try {
			// create session
			params.setKeyManager(new PublicKeyManager(userProfile.getUserId(), userProfile
					.getEncryptionKeys(), networkManager.getDataManager()));

			session = new H2HSession(params);
		} catch (IOException | NoPeerConnectionException e) {
			throw new ProcessExecutionException("Session could not be created.", e);
		}

		// set session
		networkManager.setSession(session);
	}

	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {

		// invalidate the session
		networkManager.setSession(null);
	}

}
