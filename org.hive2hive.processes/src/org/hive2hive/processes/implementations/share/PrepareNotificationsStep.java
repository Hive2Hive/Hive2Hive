package org.hive2hive.processes.implementations.share;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.process.notify.BaseNotificationMessageFactory;
import org.hive2hive.processes.framework.abstracts.ProcessStep;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.processes.implementations.context.ShareProcessContext;

/**
 * Starts the notification process that a file has been shared.
 * 
 * @author Seppi, Nico
 */
public class PrepareNotificationsStep extends ProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(PrepareNotificationsStep.class);
	private final ShareProcessContext context;
	private final String ownUserId;

	public PrepareNotificationsStep(ShareProcessContext context, String ownUserId) {
		this.context = context;
		this.ownUserId = ownUserId;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		FileTreeNode fileNode = context.getFileTreeNode();

		// create a subtree containing all children
		FileTreeNode sharedNode = new FileTreeNode(fileNode.getKeyPair(), context.consumeNewProtectionKeys());
		sharedNode.setName(fileNode.getName());
		sharedNode.getChildren().addAll(fileNode.getChildren());

		// notify all users that share this folder (already shared or newly shared)
		MetaFolder metaFolder = (MetaFolder) context.consumeMetaDocument();
		Set<String> otherUsers = new HashSet<String>(metaFolder.getUserList());
		otherUsers.remove(ownUserId);
		logger.debug(String
				.format("Sending a notification message to %s other sharing user(s) about a newly added sharing user.",
						otherUsers.size()));

		BaseNotificationMessageFactory messageFactory = new ShareFolderNotificationMessageFactory(sharedNode);
		context.provideMessageFactory(messageFactory);
		context.provideUsersToNotify(otherUsers);
	}
}
