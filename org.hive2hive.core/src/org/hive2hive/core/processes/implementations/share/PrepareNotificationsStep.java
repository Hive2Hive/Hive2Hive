package org.hive2hive.core.processes.implementations.share;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.model.UserPermission;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.implementations.context.ShareProcessContext;
import org.hive2hive.core.processes.implementations.notify.BaseNotificationMessageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Starts the notification process that a file has been shared.
 * 
 * @author Seppi, Nico
 */
public class PrepareNotificationsStep extends ProcessStep {

	private final static Logger logger = LoggerFactory.getLogger(PrepareNotificationsStep.class);

	private final ShareProcessContext context;
	private final String userId;

	public PrepareNotificationsStep(ShareProcessContext context, String userId) {
		this.context = context;
		this.userId = userId; // ownUserId
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		logger.debug(
				"Preparing a notification message to the friend '{}' and all other sharers of the shared folder '{}'.",
				context.getFriendId(), context.getFolder().getName());

		FolderIndex fileNode = (FolderIndex) context.consumeIndex();

		// create a subtree containing all children
		FolderIndex sharedNode = new FolderIndex(fileNode.getParent(), fileNode.getFileKeys(),
				fileNode.getName());
		for (Index child : fileNode.getChildren()) {
			sharedNode.addChild(child);
			child.setParent(sharedNode);
		}

		// copy all user permissions
		List<UserPermission> userPermissions = fileNode.getUserPermissions();
		UserPermission[] permissionArray = new UserPermission[userPermissions.size() + 1];
		permissionArray = userPermissions.toArray(permissionArray);
		// add the own permission
		permissionArray[permissionArray.length - 1] = new UserPermission(userId, PermissionType.WRITE);

		// if the friend receives write access, he gets the protection key
		if (context.getPermissionType() == PermissionType.WRITE) {
			logger.debug("Friend '{}' gets WRITE access to the shared folder '{}'.", context.getFriendId(),
					context.getFolder().getName());
			sharedNode.share(context.consumeNewProtectionKeys(), permissionArray);
		} else {
			logger.debug(String.format("Friend '{}' gets READ access to the shared folder '{}'.",
					context.getFriendId(), context.getFolder().getName()));
			sharedNode.share(null, permissionArray);
		}

		// remove the parent and only send the sub-tree
		sharedNode.setParent(null);

		// notify all users of the shared node
		Set<String> friends = new HashSet<String>();
		friends.addAll(fileNode.getCalculatedUserList());
		friends.remove(userId); // skip to notify myself

		BaseNotificationMessageFactory messageFactory = new ShareFolderNotificationMessageFactory(sharedNode,
				context.getUserPermission());
		context.provideMessageFactory(messageFactory);
		context.provideUsersToNotify(friends);
	}
}
