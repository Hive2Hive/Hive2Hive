package org.hive2hive.core.processes.implementations.files.delete;

import java.security.PublicKey;
import java.util.HashSet;

import org.hive2hive.core.exceptions.InvalidProcessStateException;
import org.hive2hive.core.model.IndexNode;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.implementations.context.DeleteFileProcessContext;

/**
 * Provide the needed data for the notification
 * 
 * @author Nico
 * 
 */
public class PrepareNotificationStep extends ProcessStep {

	private final DeleteFileProcessContext context;
	private final String ownUserId;

	public PrepareNotificationStep(DeleteFileProcessContext context, String ownUserId) {
		this.context = context;
		this.ownUserId = ownUserId;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		// prepare the file tree node for sending to other users
		IndexNode fileNode = context.getDeletedNode();
		PublicKey parentKey = fileNode.getParent().getFileKey();

		// provide the message factory
		context.provideMessageFactory(new DeleteNotifyMessageFactory(fileNode.getFileKey(), parentKey,
				fileNode.getName()));

		HashSet<String> users = new HashSet<String>();
		if (context.isFileInRoot()) {
			users.add(ownUserId);
		} else {
			// the parent meta document has been fetched
			if (context.consumeMetaDocument() instanceof MetaFolder) {
				MetaFolder metaFolder = (MetaFolder) context.consumeMetaDocument();
				users.addAll(metaFolder.getUserList());
			}
		}
		// provide the user list
		context.provideUsersToNotify(users);
	}
}
