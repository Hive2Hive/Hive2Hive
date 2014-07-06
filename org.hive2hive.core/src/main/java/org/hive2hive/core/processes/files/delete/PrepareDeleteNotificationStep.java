package org.hive2hive.core.processes.files.delete;

import java.security.PublicKey;
import java.util.HashSet;
import java.util.Set;

import org.hive2hive.core.model.Index;
import org.hive2hive.core.processes.context.DeleteFileProcessContext;
import org.hive2hive.processframework.abstracts.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;

/**
 * Provide the needed data for the notification
 * 
 * @author Nico
 * 
 */
public class PrepareDeleteNotificationStep extends ProcessStep {

	private final DeleteFileProcessContext context;

	public PrepareDeleteNotificationStep(DeleteFileProcessContext context) {
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		// prepare the file tree node for sending to other users
		Index fileNode = context.consumeIndex();
		PublicKey parentKey = fileNode.getParent().getFilePublicKey();

		// provide the message factory
		context.provideMessageFactory(new DeleteNotifyMessageFactory(fileNode.getFilePublicKey(), parentKey, fileNode
				.getName()));

		Set<String> users = new HashSet<String>();
		users.addAll(fileNode.getCalculatedUserList());

		// provide the user list
		context.provideUsersToNotify(users);
	}
}
