package org.hive2hive.core.process.userprofiletask.share;

import org.apache.log4j.Logger;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;

public class ShareFolderUserProfileTask extends UserProfileTask {

	private static final long serialVersionUID = -2476009828696898562L;

	private final static Logger logger = H2HLoggerFactory.getLogger(ShareFolderUserProfileTask.class);

	private final FileTreeNode fileTree;

	public ShareFolderUserProfileTask(FileTreeNode fileTree) {
		this.fileTree = fileTree;
	}

	@Override
	public void run() {
		if (this.networkManager == null) {
			logger.error("NetworkManager is not set.");
			return;
		}
		
		logger.debug("Executing a shared folder user profile task.");
		try {
			ShareFolderNotificationProcess process = new ShareFolderNotificationProcess(fileTree,
					this.networkManager);
			process.start();
			logger.debug("Started to download the shared folder.");
		} catch (NoSessionException e) {
			logger.error("Can't download the shared folder. No session set.");
		}
	}

}
