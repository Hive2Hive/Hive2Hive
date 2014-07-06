package org.hive2hive.core.processes.share;

import java.util.HashSet;
import java.util.Set;

import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.processes.context.ShareProcessContext;
import org.hive2hive.core.processes.notify.BaseNotificationMessageFactory;
import org.hive2hive.processframework.abstracts.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Starts the notification process that a file has been shared.
 * 
 * @author Seppi, Nico
 */
public class PrepareNotificationsStep extends ProcessStep {

	private static final Logger logger = LoggerFactory.getLogger(PrepareNotificationsStep.class);

	private final ShareProcessContext context;
	private final String userId;

	public PrepareNotificationsStep(ShareProcessContext context, String userId) {
		this.context = context;
		this.userId = userId; // ownUserId
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		logger.debug("Preparing a notification message to the friend '{}' and all other sharers of the shared folder '{}'.",
				context.getFriendId(), context.getFolder().getName());

		FolderIndex folderIndex = (FolderIndex) context.consumeIndex();

		// create a subtree containing all children
		FolderIndex sharedNode = new FolderIndex(folderIndex);

		// remove content protection key in case of read permission
		if (context.getPermissionType().equals(PermissionType.READ)) {
			sharedNode.setProtectionKeys(null);
		}

		// remove the parent and only send the sub-tree
		sharedNode.decoupleFromParent();

		// notify all users of the shared node
		Set<String> friends = new HashSet<String>();
		friends.addAll(folderIndex.getCalculatedUserList());
		// skip to notify myself
		friends.remove(userId);

		BaseNotificationMessageFactory messageFactory = new ShareFolderNotificationMessageFactory(sharedNode,
				context.getUserPermission());
		context.provideMessageFactory(messageFactory);
		context.provideUsersToNotify(friends);
	}
}
