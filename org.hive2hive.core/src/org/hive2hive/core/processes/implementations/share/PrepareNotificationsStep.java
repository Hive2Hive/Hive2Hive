package org.hive2hive.core.processes.implementations.share;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.implementations.context.ShareProcessContext;
import org.hive2hive.core.processes.implementations.notify.BaseNotificationMessageFactory;

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
		FolderIndex fileNode = context.getFileTreeNode();

		// create a subtree containing all children
		FolderIndex sharedNode = new FolderIndex(null, fileNode.getFileKeys(), fileNode.getName());
		sharedNode.getChildren().addAll(fileNode.getChildren());

		// if the friend receives write access, he gets the protection key
		if (context.getPermissionType() == PermissionType.WRITE) {
			sharedNode.setProtectionKeys(context.consumeNewProtectionKeys());
		}

		// notify only the new user
		Set<String> friend = new HashSet<String>(1);
		friend.add(context.getFriendId());
		logger.debug("Sending a notification message to the friend.");

		BaseNotificationMessageFactory messageFactory = new ShareFolderNotificationMessageFactory(sharedNode);
		context.provideMessageFactory(messageFactory);
		context.provideUsersToNotify(friend);
	}
}
