package org.hive2hive.core.process.share;

import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.process.common.userprofiletask.PutUserProfileTaskStep;
import org.hive2hive.core.process.userprofiletask.share.ShareFolderUserProfileTask;

/**
 * Puts a {@link ShareFolderUserProfileTask} object in the user profile task queue of a given user.
 * 
 * @author Seppi
 */
public class PutShareFolderUserProfileTaskStep extends PutUserProfileTaskStep {

	@Override
	public void start() {
		ShareFolderProcessContext context = (ShareFolderProcessContext) getProcess().getContext();
		FileTreeNode fileNode = context.getFileTreeNode();

		// create a subtree containing all children
		FileTreeNode sharedNode = new FileTreeNode(fileNode.getKeyPair(), context.getProtectionKeys());
		sharedNode.setName(fileNode.getName());
		sharedNode.getChildren().addAll(fileNode.getChildren());
		sharedNode.setProtectionKeys(fileNode.getProtectionKeys());

		ShareFolderUserProfileTask userProfileTask = new ShareFolderUserProfileTask(sharedNode);

		try {
			put(context.getFriendId(), userProfileTask, context.getSession().getKeyPair().getPublic());
			getProcess().setNextStep(new SendNotificationsStep());
		} catch (PutFailedException e) {
			getProcess().stop(e);
		}
	}

}
