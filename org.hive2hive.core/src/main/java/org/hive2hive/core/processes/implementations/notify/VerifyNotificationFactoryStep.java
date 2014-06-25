package org.hive2hive.core.processes.implementations.notify;

import java.util.HashSet;
import java.util.Set;

import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.context.interfaces.INotifyContext;

public class VerifyNotificationFactoryStep extends ProcessStep {

	private final INotifyContext context;
	private final String userId;

	public VerifyNotificationFactoryStep(INotifyContext context, String userId) {
		this.context = context;
		this.userId = userId; // own User id
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		Set<String> usersToNotify = context.consumeUsersToNotify();
		if (context.consumeMessageFactory().createUserProfileTask(userId) == null) {
			// only private notification (or none)
			usersToNotify = new HashSet<>(1);
			if (context.consumeUsersToNotify().contains(userId))
				usersToNotify.add(userId);
			else
				throw new ProcessExecutionException(
						"Users can't be notified because the UserProfileTask is null and no notification of the own user.");
		}
	}

}
