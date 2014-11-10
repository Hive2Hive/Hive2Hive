package org.hive2hive.core.processes.share;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.events.framework.interfaces.file.IFileShareEvent;
import org.hive2hive.core.events.implementations.FileShareEvent;
import org.hive2hive.core.exceptions.AbortModifyException;
import org.hive2hive.core.exceptions.Hive2HiveException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.UserPermission;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.network.data.IUserProfileModification;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.processes.files.add.AddNotificationMessageFactory;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShareFolderUserProfileTask extends UserProfileTask implements IUserProfileModification {

	private static final long serialVersionUID = -2476009828696898562L;
	private static final Logger logger = LoggerFactory.getLogger(ShareFolderUserProfileTask.class);

	private final FolderIndex sharedIndex;
	private final UserPermission addedFriend;

	public ShareFolderUserProfileTask(String sender, FolderIndex sharedIndex, UserPermission addedFriend) {
		super(sender);
		this.sharedIndex = sharedIndex;
		this.addedFriend = addedFriend;
	}

	@Override
	public void start() {
		if (this.networkManager == null) {
			logger.error("NetworkManager is not set.");
			return;
		}

		H2HSession session;
		try {
			session = networkManager.getSession();
		} catch (NoSessionException e) {
			logger.error("No user seems to be logged in.", e);
			return;
		}

		logger.debug("Executing a shared folder user profile task.");
		UserProfileManager profileManager = session.getProfileManager();
		try {
			profileManager.modifyUserProfile(getId(), this);
		} catch (Hive2HiveException e) {
			logger.error("Cannot execute the task.", e);
		}

		if (networkManager.getUserId().equals(addedFriend.getUserId())) {
			/** Case when shared with me: Notify others that files are available */
			try {
				// notify own other clients
				notifyOtherClients(new AddNotificationMessageFactory(sharedIndex, null));
				logger.debug("Notified other client that new (shared) files are available for download.");
			} catch (IllegalArgumentException | NoPeerConnectionException | InvalidProcessStateException
					| NoSessionException e) {
				logger.error("Could not notify other clients of me about the shared file.", e);
			}

			// trigger event
			IFileShareEvent event = new FileShareEvent(sharedIndex.asFile(session.getRootFile()), addedFriend, sender);
			networkManager.getEventBus().publish(event);
		}
	}

	@Override
	public void modifyUserProfile(UserProfile userProfile) throws AbortModifyException {
		if (networkManager.getUserId().equals(addedFriend.getUserId())) {
			// I'm the new sharer
			processSharedWithMe(userProfile);
		} else {
			// New sharer, but I have the file already
			logger.debug("Other user shared folder with new user '{}'.", addedFriend);
			processSharedWithOther(userProfile);
		}
	}

	private void processSharedWithMe(UserProfile userProfile) {
		// add the tree to the root node in the user profile
		userProfile.getRoot().addChild(sharedIndex);
		sharedIndex.setParent(userProfile.getRoot());
	}

	private void processSharedWithOther(UserProfile userProfile) throws AbortModifyException {
		// Add the new user to the permission list of the folder index
		FolderIndex index = (FolderIndex) userProfile.getFileById(sharedIndex.getFilePublicKey());
		if (index == null) {
			throw new AbortModifyException("I'm not the newly shared user but don't have the shared folder");
		}

		index.addUserPermissions(addedFriend);
	}
}