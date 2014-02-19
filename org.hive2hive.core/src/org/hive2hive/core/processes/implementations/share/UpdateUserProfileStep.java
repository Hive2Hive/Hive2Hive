package org.hive2hive.core.processes.implementations.share;

import org.apache.log4j.Logger;
import org.hive2hive.core.exceptions.GetFailedException;
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

	private boolean modified = false;

	public UpdateUserProfileStep(ShareProcessContext context, UserProfileManager profileManager) {
		this.context = context;
		this.profileManager = profileManager;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		logger.debug("Updating user profile for sharing.");

		try {
			UserProfile userProfile = profileManager.getUserProfile(getID(), true);
			FolderIndex folderIndex = (FolderIndex) userProfile.getFileById(context.consumeMetaDocument()
					.getId());

			if (folderIndex.isSharedOrHasSharedChildren()) {
				// TODO this is to restrictive, what about several users sharing one single folder?
				throw new ProcessExecutionException("Folder is already shared or contains an shared folder.");
			}

			// store for the notification
			context.provideIndex(folderIndex);

			// make the node shared with the new protection keys
			folderIndex.share(context.consumeNewProtectionKeys(), context.getUserPermission());

			// upload modified profile
			logger.debug("Updating the domain key in the user profile");
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
				FolderIndex fileNode = (FolderIndex) userProfile.getFileById(context.consumeMetaDocument()
						.getId());

				// unshare the fileNode
				fileNode.unshare();
				profileManager.readyToPut(userProfile, getID());
			} catch (Exception e) {
				logger.warn(String.format(
						"Rollback of updating user profile (sharing a folder) failed. exception = '%s'", e));
			}
		}
	}
}
