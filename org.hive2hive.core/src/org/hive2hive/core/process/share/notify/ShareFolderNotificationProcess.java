package org.hive2hive.core.process.share.notify;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.common.get.GetMetaDocumentStep;

public class ShareFolderNotificationProcess extends Process {

	private final ShareFolderNotificationProcessContext context;

	public ShareFolderNotificationProcess(FileTreeNode fileTreeNode, NetworkManager networkManager)
			throws NoSessionException {
		super(networkManager);

		H2HSession session = networkManager.getSession();

		context = new ShareFolderNotificationProcessContext(this, fileTreeNode, session.getProfileManager(),
				session.getFileManager());

		// 1. get meta folder of the root
		// 2. add new child (shared folder) to the root meta
		// 3. put updated root meta folder
		// 4. update user profile
		// 5. download children of shared folder
		UpdateRootMetaStep udpateRootMetaStep = new UpdateRootMetaStep();
		GetMetaDocumentStep getRootMetaStep = new GetMetaDocumentStep(fileTreeNode.getKeyPair(), udpateRootMetaStep, context);
		setNextStep(getRootMetaStep);
	}

	@Override
	public ShareFolderNotificationProcessContext getContext() {
		return context;
	}

}
