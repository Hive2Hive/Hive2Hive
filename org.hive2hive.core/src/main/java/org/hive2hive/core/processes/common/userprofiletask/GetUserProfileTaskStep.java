package org.hive2hive.core.processes.common.userprofiletask;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.NetworkContent;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.processes.context.interfaces.IUserProfileTaskContext;
import org.hive2hive.core.security.HybridEncryptedContent;
import org.hive2hive.processframework.abstracts.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A process step which gets the next {@link UserProfileTask} object of the currently logged in user.
 * 
 * @author Seppi, Nico
 */
public class GetUserProfileTaskStep extends ProcessStep {

	private static final Logger logger = LoggerFactory.getLogger(GetUserProfileTaskStep.class);

	private final IUserProfileTaskContext context;
	private String userId;

	private final NetworkManager networkManager;

	public GetUserProfileTaskStep(IUserProfileTaskContext context, NetworkManager networkManager) {
		this.networkManager = networkManager;
		if (context == null) {
			throw new IllegalArgumentException("Context can't be null.");
		}
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		userId = networkManager.getUserId();

		DataManager dataManager;
		try {
			dataManager = networkManager.getDataManager();
		} catch (NoPeerConnectionException e) {
			throw new ProcessExecutionException(e);
		}

		logger.debug("Get the next user profile task of user '{}'.", userId);
		NetworkContent content = dataManager.getUserProfileTask(userId);

		if (content == null) {
			logger.warn("Did not get an user profile task. User ID = '{}'.", userId);
			context.provideUserProfileTask(null);
		} else {
			logger.debug("Got encrypted user profile task. User ID = '{}'", userId);

			HybridEncryptedContent encrypted = (HybridEncryptedContent) content;
			PrivateKey key = null;
			try {
				key = networkManager.getSession().getKeyPair().getPrivate();
			} catch (NoSessionException e) {
				throw new ProcessExecutionException(e);
			}
			NetworkContent decrypted = null;
			try {
				decrypted = dataManager.getEncryption().decryptHybrid(encrypted, key);
			} catch (InvalidKeyException | DataLengthException | IllegalBlockSizeException | BadPaddingException
					| IllegalStateException | InvalidCipherTextException | ClassNotFoundException | IOException e) {
				throw new ProcessExecutionException("Could not decrypt user profile task.", e);
			}
			context.provideUserProfileTask((UserProfileTask) decrypted);
			logger.debug("Successfully decrypted a user profile task. User ID = '{}'.", userId);

		}
	}
}
