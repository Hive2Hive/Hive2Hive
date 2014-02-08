package org.hive2hive.core.processes.implementations.files.add;

import java.security.PublicKey;
import java.util.HashSet;
import java.util.Set;

import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.MetaFolder;
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
		FileTreeNode fileNode = context.getNewFileTreeNode();
		PublicKey parentKey = fileNode.getParent().getFileKey();
		fileNode.setParent(null);

		UploadNotificationMessageFactory messageFactory = new UploadNotificationMessageFactory(fileNode,
				parentKey);
		context.provideMessageFactory(messageFactory);

		if (context.isInRoot()) {
			// file is in root; notify only own client
			Set<String> onlyMe = new HashSet<String>(1);
			onlyMe.add(context.getH2HSession().getCredentials().getUserId());
			context.provideUsersToNotify(onlyMe);
		} else {
			MetaFolder metaFolder = (MetaFolder) context.consumeParentMetaDocument();
			Set<String> userList = metaFolder.getUserList();
			context.provideUsersToNotify(userList);
		}
	}
}
