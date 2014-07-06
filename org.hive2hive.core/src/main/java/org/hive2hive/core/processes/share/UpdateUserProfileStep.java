package org.hive2hive.core.processes.share;

import java.nio.file.Path;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.model.UserPermission;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.processes.context.ShareProcessContext;
import org.hive2hive.processframework.RollbackReason;
import org.hive2hive.processframework.abstracts.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Updates the corresponding {@link FolderIndex} in the {@link UserProfile}. Sets content protection keys,
 * user permissions and the share flag.
 * 
 * @author Nico, Seppi
 */
public class UpdateUserProfileStep extends ProcessStep {

	private static final Logger logger = LoggerFactory.getLogger(UpdateUserProfileStep.class);

	private final ShareProcessContext context;
	private final UserProfileManager profileManager;
	private final Path root;
	private final String userId;

	private boolean modified = false;

	public UpdateUserProfileStep(ShareProcessContext context, H2HSession session) throws NoSessionException {
		this.context = context;
		this.profileManager = session.getProfileManager();
		this.root = session.getRoot();
		this.userId = session.getUserId();
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		logger.debug("Updating user profile for sharing folder '{}'.", context.getFolder().getName());

		try {
			UserProfile userProfile = profileManager.getUserProfile(getID(), true);
			FolderIndex folderIndex = (FolderIndex) userProfile.getFileByPath(context.getFolder(), root);

			if (!folderIndex.canWrite()) {
				throw new ProcessExecutionException(String.format("Cannot share folder '%s' with read-only access.",
						folderIndex.getName()));
			} else if (!folderIndex.getSharedFlag() && folderIndex.isSharedOrHasSharedChildren()) {
				// restriction that disallows sharing folders within other shared folders
				throw new ProcessExecutionException(String.format(
						"Folder '%s' is already shared or contains an shared folder.", folderIndex.getName()));
			}

			// check if the folder is already shared with this user
			if (folderIndex.getCalculatedUserList().contains(context.getFriendId())) {
				throw new ProcessExecutionException(String.format("Friend '%s' already has access to folder '%s'.",
						context.getFriendId(), folderIndex.getName()));
			}

			// store for the notification
			context.provideIndex(folderIndex);

			if (folderIndex.getSharedFlag()) {
				// this if-clause allows sharing with multiple users and omits the next if-clause
				logger.debug("Sharing an already shared folder '{}' with friend '{}'.", folderIndex.getName(),
						context.getFriendId());
				folderIndex.addUserPermissions(context.getUserPermission());
			} else {
				// make the node shared with the new protection keys
				folderIndex.share(context.consumeNewProtectionKeys());
				// add read/write user permission of friend
				folderIndex.addUserPermissions(context.getUserPermission());
				// add write user permission of user itself
				folderIndex.addUserPermissions(new UserPermission(userId, PermissionType.WRITE));
			}

			// upload modified profile
			profileManager.readyToPut(userProfile, getID());

			// set modification flag needed for roll backs
			modified = true;
		} catch (GetFailedException | PutFailedException e) {
			throw new ProcessExecutionException(e);
		}
	}

	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {
		if (modified) {
			// return to original domain key and put the userProfile
			try {
				UserProfile userProfile = profileManager.getUserProfile(getID(), true);
				FolderIndex folderNode = (FolderIndex) userProfile.getFileById(context.consumeMetaFile().getId());

				// unshare the fileNode
				folderNode.unshare();

				profileManager.readyToPut(userProfile, getID());

				// reset flag
				modified = false;
			} catch (Exception e) {
				logger.warn("Rollback of updating user profile (sharing a folder) failed. Exception = '{}'.", e.getMessage());
			}
		}
	}
}
