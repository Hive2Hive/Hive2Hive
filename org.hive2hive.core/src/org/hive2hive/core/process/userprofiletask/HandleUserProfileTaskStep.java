package org.hive2hive.core.process.userprofiletask;

import org.apache.log4j.Logger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.common.userprofiletask.GetUserProfileTaskStep;
import org.hive2hive.core.process.common.userprofiletask.RemoveUserProfileTaskStep;

public class HandleUserProfileTaskStep extends ProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(HandleUserProfileTaskStep.class);

	private final UserProfileTaskQueueProcessContext context;

	public HandleUserProfileTaskStep(UserProfileTaskQueueProcessContext context) {
		if (context == null)
			throw new IllegalArgumentException("Context can't be null.");
		this.context = context;
	}

	@Override
	public void start() {
		UserProfileTask userProfileTask = context.getUserProfileTask();
		String userId = context.getProfileManager().getUserCredentials().getUserId();

		if (userProfileTask == null) {
			logger.debug(String.format(
					"No more user profile tasks in queue. Stopping handling. user id = '%s'", userId));
			// all user profile tasks are handled, stop process
			getProcess().setNextStep(null);
		} else {
			logger.debug(String.format("Executing a '%s' user profile task. user id = '%s'", userProfileTask
					.getClass().getName(), userId));
			// give the network manager reference to be able to run
			userProfileTask.setNetworkManager(getNetworkManager());
			// run the user profile task in own thread
			userProfileTask.start();

			/*
			 * Initialize next steps.
			 * 1. Remove done user profile task from network.
			 * 2. Get next user profile task.
			 * 3. Handle fetched user profile task.
			 */
			GetUserProfileTaskStep getUserProfileTaskStep = new GetUserProfileTaskStep(context, this);
			RemoveUserProfileTaskStep removeUserProfileTaskStep = new RemoveUserProfileTaskStep(context,
					getUserProfileTaskStep);
			getProcess().setNextStep(removeUserProfileTaskStep);
		}
	}

	@Override
	public void rollBack() {
		getProcess().nextRollBackStep();
	}

}
