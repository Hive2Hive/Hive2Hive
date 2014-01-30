package org.hive2hive.core.process.common.userprofiletask;

import java.security.PrivateKey;

import org.apache.log4j.Logger;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.context.IGetUserProfileTaskContext;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.HybridEncryptedContent;

/**
 * A process step which gets the next {@link UserProfileTask} object of the currently logged in user.
 * 
 * @author Seppi
 */
public class GetUserProfileTaskStep extends ProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(GetUserProfileTaskStep.class);

	private final IGetUserProfileTaskContext context;
	private final ProcessStep nextStep;

	private String userId;

	public GetUserProfileTaskStep(IGetUserProfileTaskContext context, ProcessStep nextStep) {
		if (context == null)
			throw new IllegalArgumentException("Context can't be null.");
		this.context = context;
		this.nextStep = nextStep;
	}

	@Override
	public void start() {
		try {
			userId = getNetworkManager().getSession().getCredentials().getUserId();
		} catch (NoSessionException e) {
			logger.error("No user is logged in. No session set.");
			getProcess().stop(e);
			return;
		}

		DataManager dataManager = getNetworkManager().getDataManager();
		if (dataManager == null) {
			getProcess().stop("Node is not connected.");
			return;
		}

		logger.debug(String.format("Get the next user profile task of user '%s'.", userId));
		NetworkContent content = dataManager.getUserProfileTask(userId);

		if (content == null) {
			logger.warn(String.format("Did not get an user profile task. user id = '%s'", userId));
			context.setEncryptedUserProfileTask(null);
			context.setUserProfileTask(null);
		} else {
			logger.debug(String.format("Got encrypted user profile task. user id = '%s'", userId));
			try {
				HybridEncryptedContent encrypted = (HybridEncryptedContent) content;
				context.setEncryptedUserProfileTask(encrypted);
				PrivateKey key = getNetworkManager().getSession().getKeyPair().getPrivate();
				NetworkContent decrypted = H2HEncryptionUtil.decryptHybrid(encrypted, key);
				context.setUserProfileTask((UserProfileTask) decrypted);
				logger.debug(String.format("Successfully decrypted an user profile task. user id = '%s'",
						userId));
			} catch (Exception e) {
				logger.error(String.format(
						"Cannot decrypt the user profile task. reason = '%s' user id = '%s'", e, userId));
				e.printStackTrace();
				context.setEncryptedUserProfileTask(null);
				context.setUserProfileTask(null);
			}
		}
		// continue with next step
		getProcess().setNextStep(nextStep);
	}

	@Override
	public void rollBack() {
		context.setEncryptedUserProfileTask(null);
		context.setUserProfileTask(null);
		getProcess().nextRollBackStep();
	}
}
