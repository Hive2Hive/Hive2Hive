package org.hive2hive.core.processes.implementations.share;

import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hive2hive.core.exceptions.Hive2HiveException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.processes.framework.abstracts.ProcessComponent;
import org.hive2hive.core.processes.implementations.files.util.FileRecursionUtil;

public class ShareFolderUserProfileTask extends UserProfileTask {

	private static final long serialVersionUID = -2476009828696898562L;

	private final static Logger logger = H2HLoggerFactory.getLogger(ShareFolderUserProfileTask.class);
	private final FolderIndex sharedIndex;

	public ShareFolderUserProfileTask(FolderIndex sharedIndex) {
		this.sharedIndex = sharedIndex;
	}

	@Override
	public void start() {
		if (this.networkManager == null) {
			logger.error("NetworkManager is not set.");
			return;
		}

		// TODO distinct if the receiver was the newly shared user or a user that has already this shared
		// folder and just needs to add the new user permissions
		logger.debug("Executing a shared folder user profile task.");

		try {
			/** Preparation: remove the own permission of the index because it's already in root */
			sharedIndex.removeUserPermissions(networkManager.getUserId());

			/** 1. add the tree to the root node in the user profile */
			UserProfileManager profileManager = networkManager.getSession().getProfileManager();
			String pid = UUID.randomUUID().toString();
			UserProfile userProfile = profileManager.getUserProfile(pid, true);

			// add it to the root (by definition)
			userProfile.getRoot().addChild(sharedIndex);
			sharedIndex.setParent(userProfile.getRoot());
			profileManager.readyToPut(userProfile, pid);
			logger.debug("Added the newly shared folder to the own user profile");

			/** 2. download the files that are now available */
			List<Index> fileList = FolderIndex.getIndexList(sharedIndex);
			// the folder itself is also contained, so remove it
			ProcessComponent downloadProcess = FileRecursionUtil.buildDownloadProcess(fileList,
					networkManager);
			logger.debug("Start to download " + fileList.size() + " files that have been shared with me");
			downloadProcess.start();
		} catch (Hive2HiveException e) {
			logger.error("Cannot execute the task.", e);
		}
	}
}
