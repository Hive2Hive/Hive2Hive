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
		// prepare the file tree node for sending to other users
		Index fileNode = context.getNewIndex();
		PublicKey parentKey = fileNode.getParent().getFilePublicKey();
		fileNode.setParent(null);

		UploadNotificationMessageFactory messageFactory = new UploadNotificationMessageFactory(fileNode,
				parentKey);
		context.provideMessageFactory(messageFactory);

		Index index = context.getNewIndex();
		HashSet<String> users = new HashSet<String>(0);
		users.addAll(index.getCalculatedUserList());
		context.provideUsersToNotify(users);
	}
}
