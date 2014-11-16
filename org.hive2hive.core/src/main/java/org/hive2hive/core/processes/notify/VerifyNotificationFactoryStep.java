package org.hive2hive.core.processes.notify;

import java.util.Set;

import org.hive2hive.core.processes.context.interfaces.INotifyContext;
import org.hive2hive.processframework.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;

/**
 * Verifies whether the own user is in the notifications or not.
 * 
 * @author Nico
 * 
 */
public class VerifyNotificationFactoryStep extends ProcessStep<Void> {

	private final INotifyContext context;
	// own user ID
	private final String userId;

	public VerifyNotificationFactoryStep(INotifyContext context, String userId) {
		this.setName(getClass().getName());
		this.context = context;
		this.userId = userId;
	}

	@Override
	protected Void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		
		Set<String> usersToNotify = context.consumeUsersToNotify();
		BaseNotificationMessageFactory messageFactory = context.consumeMessageFactory();
		if (messageFactory.createUserProfileTask(userId) == null && !usersToNotify.contains(userId)) {
			throw new ProcessExecutionException(this, "Users can't be notified because the UserProfileTask is null and not a notification of the own user.");
		}
		return null;
	}

}
