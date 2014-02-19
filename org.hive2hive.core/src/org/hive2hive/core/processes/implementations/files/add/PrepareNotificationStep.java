package org.hive2hive.core.processes.implementations.files.add;

import java.security.PublicKey;
import java.util.HashSet;

import org.hive2hive.core.model.Index;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.implementations.context.AddFileProcessContext;

/**
 * Provide the needed data for the notification
 * 
 * @author Nico
 * 
 */
public class PrepareNotificationStep extends ProcessStep {

	private final AddFileProcessContext context;

	public PrepareNotificationStep(AddFileProcessContext context) {
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		// get the recently added index
		Index index = context.consumeIndex();

		// get the users belonging to that index
		HashSet<String> users = new HashSet<String>();
		users.addAll(index.getCalculatedUserList());
		context.provideUsersToNotify(users);

		// prepare the file tree node for sending to other users
		PublicKey parentKey = index.getParent().getFilePublicKey();
		index.setParent(null);

		UploadNotificationMessageFactory messageFactory = new UploadNotificationMessageFactory(index,
				parentKey);
		context.provideMessageFactory(messageFactory);
	}
}
