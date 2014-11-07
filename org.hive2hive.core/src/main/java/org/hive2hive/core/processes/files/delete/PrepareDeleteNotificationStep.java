package org.hive2hive.core.processes.files.delete;

import java.util.HashSet;
import java.util.Set;

import org.hive2hive.core.model.Index;
import org.hive2hive.core.processes.context.DeleteFileProcessContext;
import org.hive2hive.processframework.abstracts.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;

/**
 * Provide the needed data for the notification
 * 
 * @author Nico, Seppi
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

		// provide the message factory
		context.provideMessageFactory(new DeleteNotifyMessageFactory(fileNode.getFilePublicKey(), fileNode.getParent()
				.getFilePublicKey(), fileNode.getName(), fileNode.isFile()));

		Set<String> users = new HashSet<String>();
		users.addAll(fileNode.getCalculatedUserList());

		// provide the user list
		context.provideUsersToNotify(users);
	}
}
