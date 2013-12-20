package org.hive2hive.core.process.login;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.ProcessStep;

public class SessionCreationStep extends ProcessStep {

	private final SessionParameters sessionParams;
	private final ProcessStep nextStep;
	private final NetworkManager networkManager;
	
	public SessionCreationStep(SessionParameters params, NetworkManager networkManager, ProcessStep nextStep) {
		this.sessionParams = params;
		this.networkManager = networkManager;
		this.nextStep = nextStep;
	}

	@Override
	public void start() {

		// create session
		sessionParams.setKeyPair(((LoginProcess) getProcess()).getContext().getUserProfile().getEncryptionKeys());
		
		H2HSession session = new H2HSession(sessionParams);
		((LoginProcess) getProcess()).getContext().setSession(session);
		
		networkManager.setSession(session);
		
		getProcess().setNextStep(nextStep);
	}

	@Override
	public void rollBack() {
		// TODO Auto-generated method stub
	}
}
