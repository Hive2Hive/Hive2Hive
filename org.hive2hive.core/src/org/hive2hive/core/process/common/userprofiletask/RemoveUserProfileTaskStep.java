package org.hive2hive.core.process.common.userprofiletask;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.context.IGetUserProfileTaskContext;

/**
 * A process step which removes a {@link UserProfileTask} object from the network.
 * 
 * @author Seppi
 */
public class RemoveUserProfileTaskStep extends ProcessStep {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(RemoveUserProfileTaskStep.class);

	private final IGetUserProfileTaskContext context;
	private final ProcessStep nextStep;

	private String userId;

	private boolean removePerformed = false;

	public RemoveUserProfileTaskStep(IGetUserProfileTaskContext context, ProcessStep nexStep) {
		this.context = context;
		this.nextStep = nexStep;
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

		DataManager dataManager;
		try {
			dataManager = getNetworkManager().getDataManager();
		} catch (NoPeerConnectionException e) {
			getProcess().stop(e);
			return;
		}

		if (context.getUserProfileTask() == null) {
			getProcess().stop("User profile task in context is null.");
			return;
		} else if (context.getEncryptedUserProfileTask() == null) {
			getProcess().stop("Encrypted user profile task in context is null.");
			return;
		}

		boolean success = dataManager.removeUserProfileTask(userId, context.getUserProfileTask()
				.getContentKey(), context.getUserProfileTask().getProtectionKey());
		removePerformed = true;

		if (success) {
			getProcess().setNextStep(nextStep);
		} else {
			getProcess().stop("Remove failed.");
		}
	}

	@Override
	public void rollBack() {
		if (!removePerformed) {
			logger.info("Noting has been removed. Skip re-adding it to the network.");
			getProcess().nextRollBackStep();
			return;
		}

		DataManager dataManager;
		try {
			dataManager = getNetworkManager().getDataManager();
		} catch (NoPeerConnectionException e) {
			logger.warn(String
					.format("Roll back of remove user profile task failed. No connection. user id = '%s' content key = '%s'",
							userId, context.getUserProfileTask().getContentKey()));
			getProcess().nextRollBackStep();
			return;
		}

		if (context.getEncryptedUserProfileTask() == null) {
			logger.warn(String
					.format("Roll back of remove user profile task failed. Encrypted user profile task is null. user id = '%s' content key = '%s'",
							userId, context.getUserProfileTask().getContentKey()));
			getProcess().nextRollBackStep();
			return;
		}

		boolean success = dataManager.putUserProfileTask(userId,
				context.getUserProfileTask().getContentKey(), context.getEncryptedUserProfileTask(), context
						.getUserProfileTask().getProtectionKey());
		if (success) {
			logger.debug(String.format(
					"Roll back of removing user profile task succeeded. user id = '%s' content key = '%s'",
					userId, context.getUserProfileTask().getContentKey()));
			getProcess().nextRollBackStep();
		} else {
			logger.warn(String
					.format("Roll back of removing user profile task failed. Re-put failed. user id = '%s' content key = '%s'",
							userId, context.getUserProfileTask().getContentKey()));
			getProcess().nextRollBackStep();
		}
	}
}
