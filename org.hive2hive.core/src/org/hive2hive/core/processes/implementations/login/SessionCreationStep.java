package org.hive2hive.core.processes.implementations.login;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Map;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.file.PersistentMetaData;
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
			// create the key manager
			PublicKeyManager keyManager = new PublicKeyManager(userProfile.getUserId(),
					userProfile.getEncryptionKeys(), networkManager.getDataManager());

			// read eventually cached keys and add them to the key manager
			PersistentMetaData metaData = FileUtil.readPersistentMetaData(params.getRoot());
			Map<String, PublicKey> publicKeyCache = metaData.getPublicKeyCache();
			for (String userId : publicKeyCache.keySet()) {
				keyManager.putPublicKey(userId, publicKeyCache.get(userId));
			}

			params.setKeyManager(keyManager);

			// create session
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
