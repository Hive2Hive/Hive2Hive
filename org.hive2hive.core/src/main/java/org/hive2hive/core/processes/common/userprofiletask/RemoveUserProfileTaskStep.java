package org.hive2hive.core.processes.common.userprofiletask;

import java.io.IOException;
import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HSession;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.versioned.HybridEncryptedContent;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.DataManager.H2HPutStatus;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.processes.context.interfaces.IUserProfileTaskContext;
import org.hive2hive.processframework.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.exceptions.ProcessRollbackException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A process step which removes a {@link UserProfileTask} object from the network.
 * 
 * @author Seppi, Nico
 */
public class RemoveUserProfileTaskStep extends ProcessStep<Void> {

	private static final Logger logger = LoggerFactory.getLogger(RemoveUserProfileTaskStep.class);

	private final IUserProfileTaskContext context;
	private NetworkManager networkManager;

	public RemoveUserProfileTaskStep(IUserProfileTaskContext context, NetworkManager networkManager) {
		this.context = context;
		this.networkManager = networkManager;
	}

	@Override
	protected Void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		String userId = networkManager.getUserId();

		DataManager dataManager;
		try {
			dataManager = networkManager.getDataManager();
		} catch (NoPeerConnectionException ex) {
			throw new ProcessExecutionException(this, ex);
		}

		if (context.consumeUserProfileTask() == null) {
			throw new ProcessExecutionException(this, "User profile task in context is null.");
		}

		boolean success = dataManager.removeUserProfileTask(userId, context.consumeUserProfileTask().getContentKey(),
				context.consumeUserProfileTask().getProtectionKey());
		if (!success) {
			throw new ProcessExecutionException(this, "Could not remove the user profile task.");
		}
		setRequiresRollback(true);
		return null;
	}

	@Override
	protected Void doRollback() throws InvalidProcessStateException, ProcessRollbackException {

		H2HSession session;
		try {
			session = networkManager.getSession();
		} catch (NoSessionException ex) {
			throw new ProcessRollbackException(this, ex);
		}

		UserProfileTask upTask = context.consumeUserProfileTask();
		String userId = networkManager.getUserId();
		DataManager dataManager;
		try {
			dataManager = networkManager.getDataManager();
		} catch (NoPeerConnectionException ex) {
			throw new ProcessRollbackException(this, ex, String.format(
					"Rollback of remove user profile task failed. No connection. User ID = '{}', Content key = '{}'.",
					userId, upTask.getContentKey()));
		}

		HybridEncryptedContent encrypted;
		try {
			encrypted = dataManager.getEncryption().encryptHybrid(upTask, session.getKeyPair().getPublic());
		} catch (DataLengthException | InvalidKeyException | IllegalStateException | InvalidCipherTextException
				| IllegalBlockSizeException | BadPaddingException | IOException ex) {
			throw new ProcessRollbackException(this, ex, "Could not encrypt the user profile task while rollback.");
		}

		encrypted.setTimeToLive(upTask.getTimeToLive());
		H2HPutStatus status = dataManager.putUserProfileTask(userId, upTask.getContentKey(), encrypted,
				upTask.getProtectionKey());
		if (status.equals(H2HPutStatus.OK)) {
			logger.debug("Rollback of removing user profile task succeeded. User ID = '{}', Content key = '{}'.", userId,
					upTask.getContentKey());
		} else {
			logger.warn("Rollback of removing user profile task failed. Re-put failed. User ID = '{}', Content key = '{}'.",
					userId, upTask.getContentKey());
		}
		return null;
	}
}
