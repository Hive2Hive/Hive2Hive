package org.hive2hive.core.processes.implementations.share;

import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hive2hive.core.exceptions.Hive2HiveException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.UserPermission;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.processes.framework.abstracts.ProcessComponent;
import org.hive2hive.core.processes.implementations.files.add.UploadNotificationMessageFactory;
import org.hive2hive.core.processes.implementations.files.util.FileRecursionUtil;

public class ShareFolderUserProfileTask extends UserProfileTask {

	private static final long serialVersionUID = -2476009828696898562L;

	private final static Logger logger = H2HLoggerFactory.getLogger(ShareFolderUserProfileTask.class);

	private final FolderIndex sharedIndex;
	private final UserPermission addedFriend;

	public ShareFolderUserProfileTask(FolderIndex sharedIndex, UserPermission addedFriend) {
		this.sharedIndex = sharedIndex;
		this.addedFriend = addedFriend;
	}

	@Override
	public void start() {
		if (this.networkManager == null) {
			logger.error("NetworkManager is not set.");
			return;
		}

		logger.debug("Executing a shared folder user profile task.");

		try {
			if (networkManager.getUserId().equals(addedFriend.getUserId())) {
				// I'm the new sharer
				processSharedWithMe();
			} else {
				// New sharer, but I have the file already
				logger.debug("Other user shared folder with new user '" + addedFriend + "'.");
				processSharedWithOther();
			}
		} catch (Hive2HiveException e) {
			logger.error("Cannot execute the task.", e);
		}

	}

	private void processSharedWithOther() throws Hive2HiveException {
		/** Add the new user to the permission list of the folder index */
		UserProfileManager profileManager = networkManager.getSession().getProfileManager();
		String pid = UUID.randomUUID().toString();
		UserProfile userProfile = profileManager.getUserProfile(pid, true);
		FolderIndex index = (FolderIndex) userProfile.getFileById(sharedIndex.getFilePublicKey());
		if (index == null) {
			throw new Hive2HiveException("I'm not the newly shared user but don't have the shared folder");
		}

		index.addUserPermissions(addedFriend);
		profileManager.readyToPut(userProfile, pid);
	}

	private void processSharedWithMe() throws Hive2HiveException {
		/** 1. add the tree to the root node in the user profile */
		UserProfileManager profileManager = networkManager.getSession().getProfileManager();
		String pid = UUID.randomUUID().toString();
		UserProfile userProfile = profileManager.getUserProfile(pid, true);

		// add it to the root (by definition)
		userProfile.getRoot().addChild(sharedIndex);
		sharedIndex.setParent(userProfile.getRoot());
		profileManager.readyToPut(userProfile, pid);
		logger.debug("Added the newly shared folder to the own user profile");

		/** 2. Notify others that files are available */
		notifyOtherClients(new UploadNotificationMessageFactory(sharedIndex, null));
		logger.debug("Notified other client that new (shared) files are available for download");

		/** 3. download the files that are now available */
		List<Index> fileList = Index.getIndexList(sharedIndex);
		// the folder itself is also contained, so remove it
		ProcessComponent downloadProcess = FileRecursionUtil.buildDownloadProcess(fileList, networkManager);
		logger.debug("Start to download " + fileList.size() + " files that have been shared with me");
		downloadProcess.start();
	}
}
