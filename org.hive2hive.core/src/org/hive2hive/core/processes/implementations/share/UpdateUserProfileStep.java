package org.hive2hive.core.processes.implementations.share;

import java.nio.file.Path;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HSession;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.context.ShareProcessContext;

public class UpdateUserProfileStep extends ProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(UpdateUserProfileStep.class);

	private final ShareProcessContext context;
	private final UserProfileManager profileManager;
	private final Path root;

	private boolean modified = false;

	public UpdateUserProfileStep(ShareProcessContext context, H2HSession session) throws NoSessionException {
		this.context = context;
		this.profileManager = session.getProfileManager();
		this.root = session.getRoot();
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		logger.debug("Updating user profile for sharing.");

		try {
			UserProfile userProfile = profileManager.getUserProfile(getID(), true);
			FolderIndex folderIndex = (FolderIndex) userProfile.getFileByPath(context.getFolder(), root);

			if (!folderIndex.canWrite()) {
				throw new ProcessExecutionException("Cannot share a folder that I have read-only access");
			} else if (!folderIndex.getSharedFlag() && folderIndex.isSharedOrHasSharedChildren()) {
				// restriction that diallows sharing folders within other shared folders
				throw new ProcessExecutionException("Folder is already shared or contains an shared folder.");
			}

			// check if the folder is already shared with this user
			if (folderIndex.getCalculatedUserList().contains(context.getFriendId())) {
				throw new ProcessExecutionException("Friend '" + context.getFriendId()
						+ "' already has access to this folder");
			}

			// store for the notification
			context.provideIndex(folderIndex);

			if (folderIndex.getSharedFlag()) {
				// this if-clause allows sharing with multiple uses and omits the next if-clause
				logger.debug("Sharing an already shared folder");
				folderIndex.addUserPermissions(context.getUserPermission());
			} else {
				// make the node shared with the new protection keys
				folderIndex.share(context.consumeNewProtectionKeys(), context.getUserPermission());
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
				FolderIndex folderNode = (FolderIndex) userProfile.getFileById(context.consumeMetaFile()
						.getId());

				// unshare the fileNode
				folderNode.unshare();
				profileManager.readyToPut(userProfile, getID());
			} catch (Exception e) {
				logger.warn(String.format(
						"Rollback of updating user profile (sharing a folder) failed. exception = '%s'", e));
			}
		}
	}
}
