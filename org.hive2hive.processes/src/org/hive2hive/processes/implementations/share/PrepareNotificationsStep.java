package org.hive2hive.processes.implementations.share;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileTreeNode;
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

	public PrepareNotificationsStep(ShareProcessContext context) {
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		FileTreeNode fileNode = context.getFileTreeNode();

		// create a subtree containing all children
		FileTreeNode sharedNode = new FileTreeNode(fileNode.getKeyPair(), context.consumeNewProtectionKeys());
		sharedNode.setName(fileNode.getName());
		sharedNode.getChildren().addAll(fileNode.getChildren());

		// notify only the new user
		Set<String> friend = new HashSet<String>(1);
		friend.add(context.getFriendId());
		logger.debug("Sending a notification message to the friend.");

		BaseNotificationMessageFactory messageFactory = new ShareFolderNotificationMessageFactory(sharedNode);
		context.provideMessageFactory(messageFactory);
		context.provideUsersToNotify(friend);
	}
}
