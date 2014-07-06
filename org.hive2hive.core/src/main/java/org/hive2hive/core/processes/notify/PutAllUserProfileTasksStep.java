package org.hive2hive.core.processes.notify;

import java.security.PublicKey;
import java.util.Map;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.processes.common.userprofiletask.PutUserProfileTaskStep;
import org.hive2hive.core.processes.context.NotifyProcessContext;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PutAllUserProfileTasksStep extends PutUserProfileTaskStep {

	private static final Logger logger = LoggerFactory.getLogger(PutAllUserProfileTasksStep.class);
	private final NotifyProcessContext context;

	public PutAllUserProfileTasksStep(NotifyProcessContext context, NetworkManager networkManager) {
		super(networkManager);
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		BaseNotificationMessageFactory messageFactory = context.consumeMessageFactory();

		UserProfileTask userProfileTask = messageFactory.createUserProfileTask(networkManager.getUserId());
		if (userProfileTask == null) {
			// skip that step
			return;
		}

		Map<String, PublicKey> userPublicKeys = context.getUserPublicKeys();
		for (String user : context.consumeUsersToNotify()) {
			if (user.equalsIgnoreCase(networkManager.getUserId())) {
				// do not put a UPtask in the own queue
				continue;
			}

			try {
				// put the profile task to the queue
				put(user, userProfileTask, userPublicKeys.get(user));
			} catch (Exception e) {
				logger.error("Could not put the user profile task to the queue of user '{}'." + user, e);
			}
		}
	}
}
