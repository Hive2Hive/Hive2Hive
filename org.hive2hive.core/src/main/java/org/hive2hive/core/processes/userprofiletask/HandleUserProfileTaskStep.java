package org.hive2hive.core.processes.userprofiletask;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.processes.common.userprofiletask.GetUserProfileTaskStep;
import org.hive2hive.core.processes.common.userprofiletask.RemoveUserProfileTaskStep;
import org.hive2hive.core.processes.context.UserProfileTaskContext;
import org.hive2hive.processframework.abstracts.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HandleUserProfileTaskStep extends ProcessStep {

	private static final Logger logger = LoggerFactory.getLogger(HandleUserProfileTaskStep.class);

	private final UserProfileTaskContext context;
	private final NetworkManager networkManager;

	public HandleUserProfileTaskStep(UserProfileTaskContext context, NetworkManager networkManager) {
		this.context = context;
		this.networkManager = networkManager;

		if (context == null) {
			throw new IllegalArgumentException("Context can't be null.");
		}
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		UserProfileTask userProfileTask = context.consumeUserProfileTask();
		String userId = networkManager.getUserId();

		if (userProfileTask == null) {
			logger.debug("No more user profile tasks in queue. Stopping handling. User ID = '{}'.", userId);
			// all user profile tasks are handled, stop process
			return;
		}

		logger.debug("Executing a '{}' user profile task. User ID = '{}'.", userProfileTask.getClass().getSimpleName(),
				userId);
		// give the network manager reference to be able to run
		userProfileTask.setNetworkManager(networkManager);
		// run the user profile task in own thread
		userProfileTask.start();

		/*
		 * Initialize next steps.
		 * 1. Remove done user profile task from network.
		 * 2. Get next user profile task.
		 * 3. Handle fetched user profile task (can be null if no next UPtask exists)
		 */
		getParent().add(new RemoveUserProfileTaskStep(context, networkManager));
		getParent().add(new GetUserProfileTaskStep(context, networkManager));
		getParent().add(new HandleUserProfileTaskStep(context, networkManager));
	}
}
