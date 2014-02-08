package org.hive2hive.core.processes.implementations.common.userprofiletask;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.apache.log4j.Logger;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideUserProfileTask;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.HybridEncryptedContent;

/**
 * A process step which gets the next {@link UserProfileTask} object of the currently logged in user.
 * 
 * @author Seppi, Nico
 */
public class GetUserProfileTaskStep extends ProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(GetUserProfileTaskStep.class);

	private final IProvideUserProfileTask context;
	private String userId;

	private final NetworkManager networkManager;

	public GetUserProfileTaskStep(IProvideUserProfileTask context, NetworkManager networkManager) {
		this.networkManager = networkManager;
		if (context == null)
			throw new IllegalArgumentException("Context can't be null.");
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

		logger.debug(String.format("Get the next user profile task of user '%s'.", userId));
		NetworkContent content = dataManager.getUserProfileTask(userId);

		if (content == null) {
			logger.warn(String.format("Did not get an user profile task. user id = '%s'", userId));
			context.provideUserProfileTask(null);
		} else {
			logger.debug(String.format("Got encrypted user profile task. user id = '%s'", userId));

			HybridEncryptedContent encrypted = (HybridEncryptedContent) content;
			PrivateKey key = null;
			try {
				key = networkManager.getSession().getKeyPair().getPrivate();
			} catch (NoSessionException e) {
				throw new ProcessExecutionException(e);
			}
			NetworkContent decrypted = null;
			try {
				decrypted = H2HEncryptionUtil.decryptHybrid(encrypted, key);
			} catch (InvalidKeyException | DataLengthException | IllegalBlockSizeException
					| BadPaddingException | IllegalStateException | InvalidCipherTextException
					| ClassNotFoundException | IOException e) {
				throw new ProcessExecutionException("Could not decrypt user profile task.", e);
			}
			context.provideUserProfileTask((UserProfileTask) decrypted);
			logger.debug(String.format("Successfully decrypted a user profile task. user id = '%s'", userId));

		}
	}
}
