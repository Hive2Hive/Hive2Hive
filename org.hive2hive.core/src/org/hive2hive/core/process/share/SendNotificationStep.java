package org.hive2hive.core.process.share;

import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.notify.INotificationMessageFactory;

public class SendNotificationStep extends ProcessStep {

	@Override
	public void start() {
		ShareFolderProcessContext context = (ShareFolderProcessContext) getProcess().getContext();
		MetaFolder metaFolder = (MetaFolder) context.getMetaDocument();
		FileTreeNode fileNode = context.getFileTreeNode();

		// create a subtree containing all children
		FileTreeNode sharedNode = new FileTreeNode(fileNode.getKeyPair());
		sharedNode.setName(fileNode.getName());
		sharedNode.getChildren().addAll(fileNode.getChildren());
		sharedNode.setDomainKeys(fileNode.getDomainKeys());
		
		// notify the other user, send him the subtree
		INotificationMessageFactory factory = new ShareFolderNotificationMessageFactory(sharedNode);
		getProcess().notfyOtherUsers(metaFolder.getUserList(), factory);
		
		getProcess().setNextStep(null);
	}

	@Override
	public void rollBack() {
		// don't do anything
		getProcess().nextRollBackStep();
	}

}
