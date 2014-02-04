package org.hive2hive.core.processes.implementations.common.userprofiletask;

import java.io.IOException;
import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeUserProfileTask;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.HybridEncryptedContent;

/**
 * A process step which removes a {@link UserProfileTask} object from the network.
 * 
 * @author Seppi, Nico
 */
public class RemoveUserProfileTaskStep extends ProcessStep {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(RemoveUserProfileTaskStep.class);

	private final IConsumeUserProfileTask context;
	private boolean removePerformed = false;
	private NetworkManager networkManager;

	public RemoveUserProfileTaskStep(IConsumeUserProfileTask context, NetworkManager networkManager) {
		this.context = context;
		this.networkManager = networkManager;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		String userId = networkManager.getUserId();

		DataManager dataManager;
		try {
			dataManager = networkManager.getDataManager();
		} catch (NoPeerConnectionException e) {
			cancel(new RollbackReason(this, "Node is not connected."));
			return;
		}

		if (context.consumeUserProfileTask() == null) {
			cancel(new RollbackReason(this, "User profile task in context is null."));
			return;
		}

		boolean success = dataManager.removeUserProfileTask(userId, context.consumeUserProfileTask()
				.getContentKey(), context.consumeUserProfileTask().getProtectionKey());
		removePerformed = true;

		if (!success) {
			cancel(new RollbackReason(this, "Could not remove the user profile task"));
		}
	}

	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {
		if (!removePerformed) {
			logger.info("Noting has been removed. Skip re-adding it to the network.");
			return;
		}

		UserProfileTask upTask = context.consumeUserProfileTask();
		HybridEncryptedContent encrypted;
		try {
			encrypted = H2HEncryptionUtil.encryptHybrid(upTask, networkManager.getPublicKey());
		} catch (DataLengthException | InvalidKeyException | IllegalStateException
				| InvalidCipherTextException | IllegalBlockSizeException | BadPaddingException | IOException e) {
			logger.error("Could not encrypt the user profile task while rollback");
			return;
		}

		String userId = networkManager.getUserId();
		DataManager dataManager;
		try {
			dataManager = networkManager.getDataManager();
		} catch (NoPeerConnectionException e) {
			logger.warn(String
					.format("Roll back of remove user profile task failed. No connection. user id = '%s' content key = '%s'",
							userId, upTask.getContentKey()));
			return;
		}

		boolean success = dataManager.putUserProfileTask(userId, upTask.getContentKey(), encrypted,
				upTask.getProtectionKey());
		if (success) {
			logger.debug(String.format(
					"Roll back of removing user profile task succeeded. user id = '%s' content key = '%s'",
					userId, upTask.getContentKey()));
		} else {
			logger.warn(String
					.format("Roll back of removing user profile task failed. Re-put failed. user id = '%s' content key = '%s'",
							userId, upTask.getContentKey()));
		}
	}
}
