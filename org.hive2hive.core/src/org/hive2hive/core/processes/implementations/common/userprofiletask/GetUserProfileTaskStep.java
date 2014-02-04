package org.hive2hive.core.processes.implementations.common.userprofiletask;

import java.security.PrivateKey;

import org.apache.log4j.Logger;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
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
	protected void doExecute() throws InvalidProcessStateException {
		userId = networkManager.getUserId();

		DataManager dataManager;
		try {
			dataManager = networkManager.getDataManager();
		} catch (NoPeerConnectionException e1) {
			cancel(new RollbackReason(this, "Node is not connected."));
			return;
		}

		logger.debug(String.format("Get the next user profile task of user '%s'.", userId));
		NetworkContent content = dataManager.getUserProfileTask(userId);

		if (content == null) {
			logger.warn(String.format("Did not get an user profile task. user id = '%s'", userId));
			context.provideUserProfileTask(null);
		} else {
			logger.debug(String.format("Got encrypted user profile task. user id = '%s'", userId));
			try {
				HybridEncryptedContent encrypted = (HybridEncryptedContent) content;
				PrivateKey key = networkManager.getSession().getKeyPair().getPrivate();
				NetworkContent decrypted = H2HEncryptionUtil.decryptHybrid(encrypted, key);
				context.provideUserProfileTask((UserProfileTask) decrypted);
				logger.debug(String.format("Successfully decrypted an user profile task. user id = '%s'",
						userId));
			} catch (Exception e) {
				logger.error(String.format(
						"Cannot decrypt the user profile task. reason = '%s' user id = '%s'", e, userId));
				context.provideUserProfileTask(null);
			}
		}
	}
}
