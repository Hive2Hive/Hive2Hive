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
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.processes.context.interfaces.IUserProfileTaskContext;
import org.hive2hive.core.security.HybridEncryptedContent;
import org.hive2hive.processframework.RollbackReason;
import org.hive2hive.processframework.abstracts.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A process step which removes a {@link UserProfileTask} object from the network.
 * 
 * @author Seppi, Nico
 */
public class RemoveUserProfileTaskStep extends ProcessStep {

	private static final Logger logger = LoggerFactory.getLogger(RemoveUserProfileTaskStep.class);

	private final IUserProfileTaskContext context;

	private boolean removePerformed = false;
	private NetworkManager networkManager;

	public RemoveUserProfileTaskStep(IUserProfileTaskContext context, NetworkManager networkManager) {
		this.context = context;
		this.networkManager = networkManager;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		String userId = networkManager.getUserId();

		DataManager dataManager;
		try {
			dataManager = networkManager.getDataManager();
		} catch (NoPeerConnectionException e) {
			throw new ProcessExecutionException(e);
		}

		if (context.consumeUserProfileTask() == null) {
			throw new ProcessExecutionException("User profile task in context is null.");
		}

		boolean success = dataManager.removeUserProfileTask(userId, context.consumeUserProfileTask().getContentKey(),
				context.consumeUserProfileTask().getProtectionKey());
		removePerformed = true;

		if (!success) {
			throw new ProcessExecutionException("Could not remove the user profile task.");
		}
	}

	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {
		if (!removePerformed) {
			logger.info("Nothing has been removed. Skip re-adding it to the network.");
			return;
		}

		H2HSession session;
		try {
			session = networkManager.getSession();
		} catch (NoSessionException e1) {
			logger.error("Could not roll back because no session.");
			return;
		}

		UserProfileTask upTask = context.consumeUserProfileTask();
		String userId = networkManager.getUserId();
		DataManager dataManager;
		try {
			dataManager = networkManager.getDataManager();
		} catch (NoPeerConnectionException e) {
			logger.warn("Rollback of remove user profile task failed. No connection. User ID = '{}', Content key = '{}'.",
					userId, upTask.getContentKey());
			return;
		}

		HybridEncryptedContent encrypted;
		try {
			encrypted = dataManager.getEncryption().encryptHybrid(upTask, session.getKeyPair().getPublic());
		} catch (DataLengthException | InvalidKeyException | IllegalStateException | InvalidCipherTextException
				| IllegalBlockSizeException | BadPaddingException | IOException e) {
			logger.error("Could not encrypt the user profile task while rollback.");
			return;
		}

		encrypted.setTimeToLive(upTask.getTimeToLive());
		boolean success = dataManager.putUserProfileTask(userId, upTask.getContentKey(), encrypted,
				upTask.getProtectionKey());
		if (success) {
			logger.debug("Rollback of removing user profile task succeeded. User ID = '{}', Content key = '{}'.", userId,
					upTask.getContentKey());
		} else {
			logger.warn("Rollback of removing user profile task failed. Re-put failed. User ID = '{}', Content key = '{}'.",
					userId, upTask.getContentKey());
		}
	}
}
