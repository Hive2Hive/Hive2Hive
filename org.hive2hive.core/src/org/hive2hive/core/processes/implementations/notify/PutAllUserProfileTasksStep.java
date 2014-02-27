package org.hive2hive.core.processes.implementations.notify;

import java.security.PublicKey;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.implementations.common.userprofiletask.PutUserProfileTaskStep;
import org.hive2hive.core.processes.implementations.context.NotifyProcessContext;

public class PutAllUserProfileTasksStep extends PutUserProfileTaskStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(PutAllUserProfileTasksStep.class);
	private final NotifyProcessContext context;

	public PutAllUserProfileTasksStep(NotifyProcessContext context, NetworkManager networkManager) {
		super(networkManager);
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		BaseNotificationMessageFactory messageFactory = context.consumeMessageFactory();

		UserProfileTask userProfileTask = messageFactory.createUserProfileTask();
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
				logger.error("Could not put the UserProfileTask to the queue of " + user, e);
			}
		}
	}
}
