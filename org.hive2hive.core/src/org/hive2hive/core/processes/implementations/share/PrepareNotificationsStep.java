package org.hive2hive.core.processes.implementations.share;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.model.UserPermission;
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
	private final String userId;

	public PrepareNotificationsStep(ShareProcessContext context, String userId) {
		this.context = context;
		this.userId = userId; // ownUserId
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		FolderIndex fileNode = context.getFileTreeNode();

		// create a subtree containing all children
		FolderIndex sharedNode = new FolderIndex(fileNode.getParent(), fileNode.getFileKeys(),
				fileNode.getName());
		sharedNode.getChildren().addAll(fileNode.getChildren());

		// copy all user permissions
		List<UserPermission> userPermissions = fileNode.getUserPermissions();
		UserPermission[] permissionArray = new UserPermission[userPermissions.size() + 1];
		permissionArray = userPermissions.toArray(permissionArray);
		// add the own permission
		permissionArray[permissionArray.length - 1] = new UserPermission(userId, PermissionType.WRITE);

		// if the friend receives write access, he gets the protection key
		if (context.getPermissionType() == PermissionType.WRITE) {
			sharedNode.share(context.consumeNewProtectionKeys(), permissionArray);
		} else {
			sharedNode.share(null, permissionArray);
		}

		// remove the parent and only send the sub-tree
		sharedNode.setParent(null);

		// notify all users of the shared node
		Set<String> friend = new HashSet<String>(1);
		friend.addAll(fileNode.getCalculatedUserList());
		logger.debug("Sending a notification message to the friend.");

		BaseNotificationMessageFactory messageFactory = new ShareFolderNotificationMessageFactory(sharedNode);
		context.provideMessageFactory(messageFactory);
		context.provideUsersToNotify(friend);
	}
}
