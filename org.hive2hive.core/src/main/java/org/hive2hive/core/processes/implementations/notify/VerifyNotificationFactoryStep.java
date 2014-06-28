package org.hive2hive.core.processes.implementations.notify;

import java.util.Set;

import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.context.interfaces.INotifyContext;

/**
 * Verifies whether the own user is in the notifications or not.
 * 
 * @author Nico
 * 
 */
public class VerifyNotificationFactoryStep extends ProcessStep {

	private final INotifyContext context;
	// own User id
	private final String userId;

	public VerifyNotificationFactoryStep(INotifyContext context, String userId) {
		this.context = context;
		this.userId = userId;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		Set<String> usersToNotify = context.consumeUsersToNotify();
		BaseNotificationMessageFactory messageFactory = context.consumeMessageFactory();
		if (messageFactory.createUserProfileTask(userId) == null && !usersToNotify.contains(userId)) {
			throw new ProcessExecutionException(
					"Users can't be notified because the UserProfileTask is null and no notification of the own user.");
		}
	}

}
