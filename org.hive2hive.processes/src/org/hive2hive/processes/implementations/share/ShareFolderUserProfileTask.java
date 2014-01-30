package org.hive2hive.processes.implementations.share;

import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hive2hive.core.exceptions.Hive2HiveException;
import org.hive2hive.core.file.FileSynchronizer;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.processes.framework.abstracts.ProcessComponent;
import org.hive2hive.processes.implementations.files.util.FileRecursionUtil;

public class ShareFolderUserProfileTask extends UserProfileTask {

	private static final long serialVersionUID = -2476009828696898562L;

	private final static Logger logger = H2HLoggerFactory.getLogger(ShareFolderUserProfileTask.class);
	private final FileTreeNode fileTree;

	public ShareFolderUserProfileTask(FileTreeNode fileTree) {
		this.fileTree = fileTree;
	}

	@Override
	public void start() {
		if (this.networkManager == null) {
			logger.error("NetworkManager is not set.");
			return;
		}

		logger.debug("Executing a shared folder user profile task.");

		try {
			/** 1. add the tree to the root node in the user profile */
			UserProfileManager profileManager = networkManager.getSession().getProfileManager();
			String pid = UUID.randomUUID().toString();
			UserProfile userProfile = profileManager.getUserProfile(pid, true);

			// add it to the root (by definition)
			userProfile.getRoot().addChild(fileTree);
			fileTree.setParent(userProfile.getRoot());

			profileManager.readyToPut(userProfile, pid);

			/** 2. download the files that are now available */
			FileSynchronizer synchronizer = new FileSynchronizer(
					networkManager.getSession().getFileManager(), userProfile);
			List<FileTreeNode> sharedFiles = synchronizer.getAddedRemotely();
			ProcessComponent downloadProcess = FileRecursionUtil.buildDownloadProcess(sharedFiles,
					networkManager);
			logger.debug("Start to download " + sharedFiles.size() + " files that have been shared with me");
			downloadProcess.start();
		} catch (Hive2HiveException e) {
			logger.error("Cannot execute the task", e);
		}
	}
}
