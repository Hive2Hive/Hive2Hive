package org.hive2hive.core.process.notify;

import java.security.PublicKey;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.process.common.userprofiletask.PutUserProfileTaskStep;

public class PutAllUserProfileTasksStep extends PutUserProfileTaskStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(PutAllUserProfileTasksStep.class);

	@Override
	public void start() {
		NotifyPeersProcessContext context = (NotifyPeersProcessContext) getProcess().getContext();
		BaseNotificationMessageFactory messageFactory = context.getMessageFactory();

		if (messageFactory.createUserProfileTask() == null) {
			// skip that step
			getProcess().setNextStep(new GetAllLocationsStep());
			return;
		}

		Map<String, PublicKey> userPublicKeys = context.getUserPublicKeys();
		for (String user : context.getUsers()) {
			if (user.equalsIgnoreCase(context.getOwnUserId())) {
				// do not put a UPtask in the own queue
				continue;
			}

			try {
				// put the profile task to the queue
				put(user, messageFactory.createUserProfileTask(), userPublicKeys.get(user));
			} catch (Exception e) {
				logger.error("Could not put the UserProfileTask to the queue of " + user, e);
			}
		}

		getProcess().setNextStep(new GetAllLocationsStep());
	}

}
