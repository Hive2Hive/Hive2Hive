package org.hive2hive.core.processes.implementations.notify;

import java.util.HashSet;
import java.util.Set;

import org.hive2hive.core.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeNotificationFactory;

public class VerifyNotificationFactoryStep extends ProcessStep {

	private final IConsumeNotificationFactory context;
	private final String userId;

	public VerifyNotificationFactoryStep(IConsumeNotificationFactory context, String userId) {
		this.context = context;
		this.userId = userId;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		Set<String> usersToNotify = context.consumeUsersToNotify();
		if (context.consumeMessageFactory().createUserProfileTask() == null) {
			// only private notification (or none)
			usersToNotify = new HashSet<>(1);
			if (context.consumeUsersToNotify().contains(userId))
				usersToNotify.add(userId);
			else
				cancel(new RollbackReason(this,
						"Users can't be notified because the UserProfileTask is null and no notification of the own user"));
		}
	}

}
