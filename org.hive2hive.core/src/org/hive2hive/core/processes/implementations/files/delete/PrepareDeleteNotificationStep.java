package org.hive2hive.core.processes.implementations.files.delete;

import java.security.PublicKey;
import java.util.HashSet;

import org.hive2hive.core.model.Index;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.implementations.context.DeleteFileProcessContext;

/**
 * Provide the needed data for the notification
 * 
 * @author Nico
 * 
 */
public class PrepareDeleteNotificationStep extends ProcessStep {

	private final DeleteFileProcessContext context;
	private final String ownUserId;

	public PrepareDeleteNotificationStep(DeleteFileProcessContext context, String ownUserId) {
		this.context = context;
		this.ownUserId = ownUserId;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		// prepare the file tree node for sending to other users
		Index fileNode = context.getDeletedIndex();
		PublicKey parentKey = fileNode.getParent().getFilePublicKey();

		// provide the message factory
		context.provideMessageFactory(new DeleteNotifyMessageFactory(fileNode.getFilePublicKey(), parentKey,
				fileNode.getName()));

		HashSet<String> users = new HashSet<String>();
		if (context.isFileInRoot()) {
			users.add(ownUserId);
		} else {
			// TODO: get the users from the index
		}
		// provide the user list
		context.provideUsersToNotify(users);
	}
}
